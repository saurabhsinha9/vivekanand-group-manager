package com.vivekanand.manager.backup;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "backup")
public class BackupProperties {

    private String localDir;
    private String remoteName;
    private String remoteFolder;
    private int retentionDays;
    private String cron;
    private String rcloneConfigPath;
}
