package com.vivekanand.manager.backup;

import com.vivekanand.manager.backup.BackupManager;
import com.vivekanand.manager.backup.BackupProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackupScheduler {

    private final BackupManager backupManager;
    private final BackupProperties backupProperties;

    public BackupScheduler(BackupManager backupManager, BackupProperties backupProperties) {
        this.backupManager = backupManager;
        this.backupProperties = backupProperties;
    }

    @Scheduled(cron = "#{@backupProperties.cron}")
    public void scheduledBackup() {
        try {
            backupManager.backupNow();
        } catch (Exception e) {
            e.printStackTrace(); // replace with proper logging
        }
    }
}
