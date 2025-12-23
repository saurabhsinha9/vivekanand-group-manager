package com.vivekanand.manager.backup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupFile = Path.of(backupProperties.getLocalDir(), "backup_" + timestamp + ".sql.gz");
        Files.createDirectories(backupFile.getParent());

        String command = String.format(
                "PGPASSWORD='%s' pg_dump -U %s %s | gzip > %s",
                dbPass, dbUser, dbName, backupFile
        );
        Process process = new ProcessBuilder("bash", "-c", command).inheritIO().start();
        if (process.waitFor() != 0) throw new RuntimeException("PostgreSQL backup failed");

        // Upload & cleanup
        rcloneUploadAndCleanup(backupFile);

        return backupFile.toString();
    }

    private void rcloneUploadAndCleanup(Path backupFile) throws Exception {
        String rcloneConfig = backupProperties.getRcloneConfigPath() != null
                ? "--config " + backupProperties.getRcloneConfigPath()
                : "";

        String remoteTarget = backupProperties.getRemoteName() + ":" + backupProperties.getRemoteFolder();

        // Upload
        Process rcloneUpload = new ProcessBuilder("bash", "-c",
                "rclone " + rcloneConfig + " copy " + backupFile + " " + remoteTarget)
                .inheritIO().start();
        rcloneUpload.waitFor();

        // Cleanup old backups
        cleanupOldLocalBackups();
        cleanupOldRemoteBackups(rcloneConfig);
    }

    private void cleanupOldLocalBackups() throws Exception {
        Path dir = Path.of(backupProperties.getLocalDir());
        if (!Files.exists(dir)) return;

        Files.list(dir)
                .filter(Files::isRegularFile)
                .filter(f -> f.getFileName().toString().endsWith(".sql.gz"))
                .forEach(f -> {
                    try {
                        long ageMillis = System.currentTimeMillis() - Files.getLastModifiedTime(f).toMillis();
                        if (ageMillis > backupProperties.getRetentionDays() * 24L * 3600 * 1000) {
                            Files.deleteIfExists(f);
                            System.out.println("Deleted old local backup: " + f);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void cleanupOldRemoteBackups(String rcloneConfig) throws Exception {
        String remoteTarget = backupProperties.getRemoteName() + ":" + backupProperties.getRemoteFolder();
        String cmd = String.format("rclone %s delete --min-age %dd %s", rcloneConfig, backupProperties.getRetentionDays(), remoteTarget);
        Process rcloneDelete = new ProcessBuilder("bash", "-c", cmd).inheritIO().start();
        rcloneDelete.waitFor();
    }
}
