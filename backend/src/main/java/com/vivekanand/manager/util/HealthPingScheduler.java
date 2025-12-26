package com.vivekanand.manager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class HealthPingScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthPingScheduler.class);

    private final RestTemplate restTemplate;
    private final String healthUrl;

    public HealthPingScheduler(
            RestTemplate restTemplate,
            @Value("${ping.health.url}")
            String healthUrl) {
        this.restTemplate = restTemplate;
        this.healthUrl = healthUrl;
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void pingHealth() {
        try {
            String body = restTemplate.getForObject(healthUrl, String.class);
            log.info("Health ping OK: {}", body);
        } catch (RestClientException ex) {
            log.warn("Health ping failed: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error during health ping", ex);
        }
    }
}
