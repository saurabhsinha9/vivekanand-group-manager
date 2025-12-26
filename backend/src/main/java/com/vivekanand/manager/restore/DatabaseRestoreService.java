package com.vivekanand.manager.restore;

import com.vivekanand.manager.backup.BackupProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

@Service
public class DatabaseRestoreService {

    private final JdbcTemplate jdbcTemplate;
    private final BackupProperties backupProperties;

    public DatabaseRestoreService(JdbcTemplate jdbcTemplate,
                                  BackupProperties backupProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.backupProperties = backupProperties;
    }

    /**
     * Check if the database is empty (no tables in public schema)
     */
    public boolean isDatabaseEmpty() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public'",
                Integer.class
        );
        return count == null || count == 0;
    }

    /**
     * Restore the latest backup SQL file from Rclone
     */
    public void restoreLatestBackup() throws Exception {
        Path localDir = Path.of(backupProperties.getLocalDir());
        Files.createDirectories(localDir);

        String rcloneConfig = backupProperties.getRcloneConfigPath() != null
                ? "--config " + backupProperties.getRcloneConfigPath()
                : "";

        String remoteTarget =
                backupProperties.getRemoteName() + ":" + backupProperties.getRemoteFolder();

        // Download all SQL files
        String downloadCmd = "rclone " + rcloneConfig +
                " copy --include 'backup_*.sql' " +
                remoteTarget + " " + localDir;
        runCommand(downloadCmd);

        // Pick the latest backup
        Optional<Path> latestBackup = Files.list(localDir)
                .filter(f -> f.getFileName().toString().endsWith(".sql"))
                .max(Comparator.comparingLong(f -> f.toFile().lastModified()));

        if (latestBackup.isEmpty()) {
            throw new RuntimeException("No backup SQL file found");
        }

        restoreSqlFile(latestBackup.get());
    }

    /**
     * Restore SQL file using JDBC
     */
    @Transactional
    public void restoreSqlFile(Path sqlFile) throws Exception {
        System.out.println("[RESTORE] Restoring from file: " + sqlFile);

        try (BufferedReader reader = Files.newBufferedReader(sqlFile)) {
            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("--")) continue;

                sql.append(line).append(" ");

                if (line.endsWith(";")) {
                    jdbcTemplate.execute(sql.toString());
                    sql.setLength(0);
                }
            }
        }

        System.out.println("[RESTORE] Restore completed successfully");
    }

    /**
     * Run shell command (for Rclone)
     */
    private void runCommand(String command) throws Exception {
        System.out.println("[CMD] " + command);

        Process process = new ProcessBuilder("bash", "-c", command)
                .redirectErrorStream(true)
                .start();

        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[CMD] " + line);
            }
        }

        int exit = process.waitFor();
        if (exit != 0) {
            throw new RuntimeException("Command failed with exit code " + exit);
        }
    }
}
