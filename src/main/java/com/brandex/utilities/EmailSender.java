package com.brandex.utilities;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

// This utility class was created using the assistance of an AI language model.
// It provides basic email sending functionality.
public class EmailSender {

    private static final String FROM = ConfigLoader.get("email.user");
    private static final String PASSWORD = ConfigLoader.get("email.password");

    // Sends an email to a specified recipient.
    public static void send(String toEmail, String subject, String body) throws Exception {
        int maxRetries = 3;
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.connectiontimeout", "5000"); // 5s timeout
                props.put("mail.smtp.timeout", "5000");

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM, PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
                return; // Success!
            } catch (Exception e) {
                attempt++;
                lastException = e;
                System.err.println("Email attempt " + attempt + " failed for " + toEmail + ": " + e.getMessage());
                if (attempt < maxRetries) {
                    Thread.sleep(5000); // Wait 5s before retry
                }
            }
        }
        throw new Exception(
                "Failed to send email after " + maxRetries + " attempts. Last error: " + lastException.getMessage());
    }

    // Sends an email asynchronously.
    public static void sendAsync(String toEmail, String subject, String body, Runnable onSuccess,
            java.util.function.Consumer<String> onFailure) {
        Thread thread = new Thread(() -> {
            try {
                send(toEmail, subject, body);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {
                if (onFailure != null) {
                    onFailure.accept(e.getMessage());
                } else {
                    System.err.println("Async email failed permanently: " + e.getMessage());
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
