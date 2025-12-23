package com.vivekanand.manager.backup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BackupManager {

    private final List<BackupService> backupServices;
    private final String dbType;

    @Autowired
    public BackupManager(List<BackupService> backupServices,
                         @Value("${spring.datasource.url}") String dbUrl) {
        this.backupServices = backupServices;
        this.dbType = detectDbType(dbUrl);
    }

    public String backupNow() throws Exception {
        return backupServices.stream()
                .filter(s -> s.getDatabaseType().equalsIgnoreCase(dbType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No backup service found for DB: " + dbType))
                .backupNow();
    }

    private String detectDbType(String url) {
        if (url.startsWith("jdbc:postgresql:")) return "postgresql";
        if (url.startsWith("jdbc:mysql:")) return "mysql";
        throw new RuntimeException("Unsupported datasource URL: " + url);
    }
}
