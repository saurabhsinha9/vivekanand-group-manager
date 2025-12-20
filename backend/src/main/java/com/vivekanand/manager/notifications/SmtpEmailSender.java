
package com.vivekanand.manager.notifications;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    public SmtpEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String subject, String bodyHtml) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(stripHtml(bodyHtml));
            mailSender.send(mail);
        } catch (Exception ex) {
            System.err.println("SMTP send failed: " + ex.getMessage());
        }
    }

    private static String stripHtml(String html) {
        return html == null ? "" : html.replaceAll("<[^>]*>", "");
    }
}
