package com.vivekanand.manager.backup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostgresBackupService implements BackupService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    private final BackupProperties backupProperties;

    public PostgresBackupService(BackupProperties backupProperties) {
        this.backupProperties = backupProperties;
    }

    @Override
    public String getDatabaseType() {
        return "postgresql";
    }

    @Override
    public String backupNow() throws Exception {

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        Path backupFile = Path.of(
                backupProperties.getLocalDir(),
                "backup_" + timestamp + ".sql"
        );

        Files.createDirectories(backupFile.getParent());

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             BufferedWriter writer = Files.newBufferedWriter(
                     backupFile,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING
             )) {

            writer.write("-- PostgreSQL JDBC Backup\n");
            writer.write("-- Generated at " + LocalDateTime.now() + "\n\n");

            List<String> tables = fetchPublicTables(conn);

            for (String table : tables) {
                backupTable(conn, writer, table);
            }
        }

        rcloneUploadAndCleanup(backupFile);
        return backupFile.toString();
    }

    // ---------------- INTERNAL METHODS ----------------

    private List<String> fetchPublicTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' " +
                        "ORDER BY table_name")) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
        }
        return tables;
    }

    private void backupTable(Connection conn, BufferedWriter writer, String table)
            throws SQLException, IOException {

        writer.write("-- ----------------------------\n");
        writer.write("-- Table: " + table + "\n");
        writer.write("-- ----------------------------\n");
        writer.write("TRUNCATE TABLE " + table + " CASCADE;\n");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // Prepare column names for INSERT
            StringBuilder columnNames = new StringBuilder();
            columnNames.append("(");
            for (int i = 1; i <= columnCount; i++) {
                columnNames.append(meta.getColumnName(i));
                if (i < columnCount) columnNames.append(", ");
            }
            columnNames.append(")");

            while (rs.next()) {
                StringBuilder insert = new StringBuilder();
                insert.append("INSERT INTO ").append(table).append(" ")
                        .append(columnNames).append(" VALUES (");

                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);

                    if (value == null) {
                        insert.append("NULL");
                    } else if (value instanceof Number || value instanceof Boolean) {
                        insert.append(value);
                    } else {
                        insert.append("'")
                                .append(value.toString().replace("'", "''"))
                                .append("'");
                    }

                    if (i < columnCount) insert.append(", ");
                }
                insert.append(");\n");
                writer.write(insert.toString());
            }
        }
        writer.write("\n");
    }

    private void rcloneUploadAndCleanup(Path backupFile) throws Exception {

        String rcloneConfig = backupProperties.getRcloneConfigPath() != null
                ? "--config " + backupProperties.getRcloneConfigPath()
                : "";

        String remoteTarget = backupProperties.getRemoteName()
                + ":" + backupProperties.getRemoteFolder();

        Process upload = new ProcessBuilder(
                "bash", "-c",
                "rclone " + rcloneConfig + " copy " +
                        backupFile + " " + remoteTarget
        ).inheritIO().start();

        upload.waitFor();

        cleanupOldLocalBackups();
        cleanupOldRemoteBackups(rcloneConfig);
    }

    private void cleanupOldLocalBackups() throws IOException {
        Path dir = Path.of(backupProperties.getLocalDir());
        if (!Files.exists(dir)) return;

        long maxAgeMs = backupProperties.getRetentionDays()
                * 24L * 60 * 60 * 1000;

        Files.list(dir)
                .filter(p -> p.getFileName().toString().endsWith(".sql"))
                .forEach(p -> {
                    try {
                        if (System.currentTimeMillis()
                                - Files.getLastModifiedTime(p).toMillis() > maxAgeMs) {
                            Files.deleteIfExists(p);
                        }
                    } catch (Exception ignored) {
                    }
                });
    }

    private void cleanupOldRemoteBackups(String rcloneConfig) throws Exception {
        String remoteTarget = backupProperties.getRemoteName()
                + ":" + backupProperties.getRemoteFolder();

        Process delete = new ProcessBuilder(
                "bash", "-c",
                "rclone " + rcloneConfig +
                        " delete --min-age " +
                        backupProperties.getRetentionDays() + "d " +
                        remoteTarget
        ).inheritIO().start();

        delete.waitFor();
    }
}
