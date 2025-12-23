package com.vivekanand.manager.backup;

import com.vivekanand.manager.backup.BackupManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/backup")
public class BackupController {

    private final BackupManager backupManager;

    public BackupController(BackupManager backupManager) {
        this.backupManager = backupManager;
    }

    @PostMapping("/run")
    public String runBackup() throws Exception {
        return backupManager.backupNow();
    }
}
