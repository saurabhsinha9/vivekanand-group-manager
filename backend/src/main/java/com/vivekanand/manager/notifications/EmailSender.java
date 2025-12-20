package com.vivekanand.manager.notifications;

public interface EmailSender {
    void send(String to, String subject, String bodyHtml);
}
