package com.vivekanand.manager.notifications;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "http", matchIfMissing = true)
public class HttpEmailSender implements EmailSender {

    private final RestTemplate http;
    private final Environment env;

    public HttpEmailSender(RestTemplate http, Environment env) {
        this.http = http;
        this.env = env;
    }

    @Override
    public void send(String to, String subject, String bodyHtml) {
        String url   = env.getProperty("email.provider.url");
        String apiKey= env.getProperty("email.provider.key");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("HTTP email: missing API_KEY");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Prefer Authorization: Bearer (your PHP supports both)
            headers.setBearerAuth(apiKey);
            // Or use the custom header:
            // headers.set("X-API-KEY", apiKey);

            Map<String, Object> payload = Map.of(
                    "to", List.of(to),
                    "subject", subject,
                    "body", bodyHtml
            );

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
            ResponseEntity<String> res = http.postForEntity(url, req, String.class);

            if (!res.getStatusCode().is2xxSuccessful()) {
                System.err.println("HTTP email failed: " + res.getStatusCode() + " " + res.getBody());
            }
        } catch (Exception ex) {
            System.err.println("HTTP email exception: " + ex.getMessage());
        }
    }
}

