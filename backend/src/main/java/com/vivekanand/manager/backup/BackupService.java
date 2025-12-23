package com.vivekanand.manager.backup;

public interface BackupService {

    /**
     * Perform backup immediately
     *
     * @return path of local backup file
     * @throws Exception if backup fails
     */
    String backupNow() throws Exception;

    /**
     * Returns database type supported by this service (e.g., "postgresql", "mysql")
     */
    String getDatabaseType();
}
