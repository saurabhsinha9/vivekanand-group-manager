
package com.vivekanand.manager.notifications;

import com.vivekanand.manager.members.MemberRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class NotificationService {
    private final MemberRepository members;
    private final EmailSender emailSender;   // <-- provider-agnostic
    private final RestTemplate http;
    private final Environment env;

    public NotificationService(MemberRepository members, EmailSender emailSender,
                               RestTemplate http, Environment env) {
        this.members = members;
        this.emailSender = emailSender;
        this.http = http;
        this.env = env;
    }

    public void broadcast(String message) {
        var list = members.findByActiveTrue();
        for (var m : list) {
            if (Boolean.TRUE.equals(m.getNotifyEmail()) &&
                    m.getEmail() != null && !m.getEmail().isBlank()) {

                String html = "<p>" + escapeHtml(message) + "</p>";
                emailSender.send(m.getEmail(), "Vivekanand Group Announcement", html);
            }
            if (Boolean.TRUE.equals(m.getNotifyWhatsapp()) &&
                    m.getPhone() != null && !m.getPhone().isBlank()) {
                sendWhatsapp(m.getPhone(), message);
            }
        }
    }

    private void sendWhatsapp(String to, String message) {
        String provider = env.getProperty("whatsapp.provider", "meta");
        if ("twilio".equalsIgnoreCase(provider)) {
            String sid = env.getProperty("whatsapp.twilio.accountSid");
            String token = env.getProperty("whatsapp.twilio.authToken");
            String from = env.getProperty("whatsapp.twilio.whatsappFrom");
            if (sid == null || token == null || from == null) return;
            try {
                HttpHeaders h = new HttpHeaders();
                h.setBasicAuth(sid, token);
                h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                String body = "From=whatsapp:" + from + "&To=whatsapp:" + to +
                        "&Body=" + message.replace(" ", "+");
                http.postForEntity(
                        "https://api.twilio.com/2010-04-01/Accounts/" + sid + "/Messages.json",
                        new HttpEntity<>(body, h),
                        String.class
                );
            } catch (Exception ignored) { }
        } else {
            String phoneNumberId = env.getProperty("whatsapp.meta.phoneNumberId");
            String accessToken   = env.getProperty("whatsapp.meta.accessToken");
            if (phoneNumberId == null || accessToken == null) return;
            try {
                HttpHeaders h = new HttpHeaders();
                h.setBearerAuth(accessToken);
                h.setContentType(MediaType.APPLICATION_JSON);
                Map<String, Object> payload = Map.of(
                        "messaging_product", "whatsapp",
                        "to", to,
                        "type", "text",
                        "text", Map.of("body", message)
                );
                http.postForEntity(
                        "https://graph.facebook.com/v21.0/" + phoneNumberId + "/messages",
                        new HttpEntity<>(payload, h),
                        String.class
                );
            } catch (Exception ignored) { }
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
