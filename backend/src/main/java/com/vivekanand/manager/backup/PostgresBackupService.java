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
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupFile = Path.of(backupProperties.getLocalDir(), "backup_" + timestamp + ".sql");
        Files.createDirectories(backupFile.getParent());

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             BufferedWriter writer = Files.newBufferedWriter(backupFile, StandardOpenOption.CREATE)) {

            // 1️⃣ Export tables and data
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = conn.getMetaData().getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tables.add(tableName);

                    writer.write("-- Table: " + tableName + "\n");
                    writer.write("TRUNCATE TABLE " + tableName + " CASCADE;\n\n");

                    // Export data
                    try (Statement stmt = conn.createStatement();
                         ResultSet rsData = stmt.executeQuery("SELECT * FROM " + tableName)) {
                        ResultSetMetaData meta = rsData.getMetaData();
                        while (rsData.next()) {
                            StringBuilder sb = new StringBuilder("INSERT INTO " + tableName + " VALUES(");
                            for (int i = 1; i <= meta.getColumnCount(); i++) {
                                Object val = rsData.getObject(i);
                                if (val == null) sb.append("NULL");
                                else sb.append("'").append(val.toString().replace("'", "''")).append("'");
                                if (i < meta.getColumnCount()) sb.append(",");
                            }
                            sb.append(");\n");
                            writer.write(sb.toString());
                        }
                    }
                    writer.write("\n");
                }
            }

            // 2️⃣ Export sequences
            try (Statement stmt = conn.createStatement();
                 ResultSet rsSeq = stmt.executeQuery(
                         "SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema='public'")) {
                while (rsSeq.next()) {
                    String seq = rsSeq.getString("sequence_name");
                    writer.write("-- Sequence: " + seq + "\n");
                    try (ResultSet rsVal = stmt.executeQuery("SELECT last_value, is_called FROM " + seq)) {
                        if (rsVal.next()) {
                            long val = rsVal.getLong("last_value");
                            boolean called = rsVal.getBoolean("is_called");
                            writer.write("ALTER SEQUENCE " + seq + " RESTART WITH " + (called ? val + 1 : val) + ";\n\n");
                        }
                    }
                }
            }

            // 3️⃣ Export indexes
            try (Statement stmt = conn.createStatement();
                 ResultSet rsIdx = stmt.executeQuery(
                         "SELECT indexname, indexdef FROM pg_indexes WHERE schemaname='public'")) {
                while (rsIdx.next()) {
                    writer.write("-- Index: " + rsIdx.getString("indexname") + "\n");
                    writer.write(rsIdx.getString("indexdef") + ";\n\n");
                }
            }

            // 4️⃣ Export foreign keys
            try (Statement stmt = conn.createStatement();
                 ResultSet rsFk = stmt.executeQuery(
                         "SELECT conname, pg_get_constraintdef(oid) as definition FROM pg_constraint " +
                                 "WHERE connamespace = 'public'::regnamespace AND contype='f'")) {
                while (rsFk.next()) {
                    writer.write("-- Foreign key: " + rsFk.getString("conname") + "\n");
                    writer.write("ALTER TABLE ONLY " + rsFk.getString("definition") + ";\n\n");
                }
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Postgres backup failed: " + e.getMessage(), e);
        }

        // Upload to remote and cleanup old backups
        rcloneUploadAndCleanup(backupFile);

        return backupFile.toString();
    }

    private void rcloneUploadAndCleanup(Path backupFile) throws Exception {
        String rcloneConfig = backupProperties.getRcloneConfigPath() != null
                ? "--config " + backupProperties.getRcloneConfigPath() : "";
        String remoteTarget = backupProperties.getRemoteName() + ":" + backupProperties.getRemoteFolder();

        Process rcloneUpload = new ProcessBuilder("bash", "-c",
                "rclone " + rcloneConfig + " copy " + backupFile + " " + remoteTarget)
                .inheritIO().start();
        rcloneUpload.waitFor();

        cleanupOldLocalBackups();
        cleanupOldRemoteBackups(rcloneConfig);
    }

    private void cleanupOldLocalBackups() {
        Path dir = Path.of(backupProperties.getLocalDir());
        if (!Files.exists(dir)) return;

        try {
            Files.list(dir)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(".sql"))
                    .forEach(f -> {
                        try {
                            long ageMillis = System.currentTimeMillis() - Files.getLastModifiedTime(f).toMillis();
                            if (ageMillis > backupProperties.getRetentionDays() * 24L * 3600 * 1000) {
                                Files.deleteIfExists(f);
                                System.out.println("Deleted old local backup: " + f);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanupOldRemoteBackups(String rcloneConfig) throws Exception {
        String remoteTarget = backupProperties.getRemoteName() + ":" + backupProperties.getRemoteFolder();
        String cmd = String.format("rclone %s delete --min-age %dd %s",
                rcloneConfig, backupProperties.getRetentionDays(), remoteTarget);
        Process rcloneDelete = new ProcessBuilder("bash", "-c", cmd).inheritIO().start();
        rcloneDelete.waitFor();
    }
}
