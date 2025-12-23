package com.vivekanand.manager.backup;

import com.vivekanand.manager.backup.BackupManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/backup")
public class BackupController {

    private final BackupManager backupManager;

    public BackupController(BackupManager backupManager) {
        this.backupManager = backupManager;
    }

    @PostMapping("/run")
    public String runBackup() throws Exception {
        backupManager.backupNowAsync();
        return "Backup started in background";
    }
}
