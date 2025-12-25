package com.vivekanand.manager.backup;

public interface BackupService {
    String backupNow() throws Exception;
    String getDatabaseType();
}
