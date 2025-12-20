package com.vivekanand.manager.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "cloudinary")
    public Cloudinary cloudinary(
            @Value("${cloudinary.cloudName}") String cloudName,
            @Value("${cloudinary.apiKey}") String apiKey,
            @Value("${cloudinary.apiSecret}") String apiSecret
    ) {
        Map<String, Object> config = ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        );
        return new Cloudinary(config);
    }
}
