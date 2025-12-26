package com.vivekanand.manager.restore;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class StartupRestoreRunner implements ApplicationRunner {

    private final DatabaseRestoreService restoreService;

    public StartupRestoreRunner(DatabaseRestoreService restoreService) {
        this.restoreService = restoreService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (restoreService.isDatabaseEmpty()) {
                System.out.println("[RESTORE] Empty database detected. Starting restore...");
                restoreService.restoreLatestBackup();
                System.out.println("[RESTORE] Database restore completed");
            } else {
                System.out.println("[RESTORE] Database already initialized. Skipping restore.");
            }
        } catch (Exception e) {
            System.err.println("[RESTORE] Restore failed");
            e.printStackTrace();
            // IMPORTANT: Do NOT crash app
        }
    }
}
