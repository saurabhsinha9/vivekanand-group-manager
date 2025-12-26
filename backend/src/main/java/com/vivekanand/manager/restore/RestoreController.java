package com.vivekanand.manager.restore;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/restore")
public class RestoreController {
    private final DatabaseRestoreService restoreService;

    public RestoreController(DatabaseRestoreService restoreService) {
        this.restoreService = restoreService;
    }

    @PostMapping("/run")
    public String runRestore() throws Exception {
        restoreService.restoreLatestBackup();
        return "Restore triggered";
    }
}
