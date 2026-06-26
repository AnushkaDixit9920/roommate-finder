package com.roommatefinder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class EmailService {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String FROM_EMAIL = "anushkadixit9920@gmail.com";
    private static final String FROM_NAME = "RoommateFinder";

    // NOT @Async — must succeed or registration fails with a visible error
    public void sendVerificationEmail(String toEmail, String name, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        String body = buildEmailJson(toEmail, name,
                "Verify your RoommateFinder account",
                "Hi " + name + ",\\n\\nWelcome to RoommateFinder! Please verify your email to get started.\\n\\n" +
                        "Click here to verify: " + link + "\\n\\nThis link expires in 24 hours.\\n\\n— RoommateFinder Team");
        sendHttp(toEmail, body);
        log.info("Verification email sent to {}", toEmail);
    }

    @Async
    public void sendMatchRequestEmail(String toEmail, String receiverName, String senderName, int score) {
        try {
            String body = buildEmailJson(toEmail, receiverName,
                    "You have a new roommate request! \uD83C\uDFE0",
                    "Hi " + receiverName + ",\\n\\n" + senderName + " wants to connect with you as a roommate!\\n\\n" +
                            "Compatibility Score: " + score + "%\\n\\n" +
                            "Login to view their profile and respond: " + frontendUrl + "/dashboard\\n\\n— RoommateFinder Team");
            sendHttp(toEmail, body);
        } catch (Exception e) {
            log.error("Failed to send match request email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendMatchAcceptedEmail(String toEmail, String senderName, String receiverName) {
        try {
            String body = buildEmailJson(toEmail, senderName,
                    "Your roommate request was accepted! \uD83C\uDF89",
                    "Hi " + senderName + ",\\n\\nGreat news! " + receiverName + " accepted your roommate request.\\n\\n" +
                            "Login to view their contact details: " + frontendUrl + "/dashboard\\n\\n— RoommateFinder Team");
            sendHttp(toEmail, body);
        } catch (Exception e) {
            log.error("Failed to send match accepted email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) {
        try {
            String link = frontendUrl + "/reset-password?token=" + resetToken;
            String body = buildEmailJson(toEmail, name,
                    "Reset your RoommateFinder password",
                    "Hi " + name + ",\\n\\nClick the link below to reset your password (expires in 1 hour):\\n" +
                            link + "\\n\\nIf you did not request this, ignore this email.\\n\\n— RoommateFinder Team");
            sendHttp(toEmail, body);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildEmailJson(String toEmail, String toName, String subject, String textContent) {
        return """
            {
              "sender": { "name": "%s", "email": "%s" },
              "to": [{ "email": "%s", "name": "%s" }],
              "subject": "%s",
              "textContent": "%s"
            }
            """.formatted(FROM_NAME, FROM_EMAIL, toEmail, toName, subject, textContent);
    }

    private void sendHttp(String toEmail, String jsonBody) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_URL))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Brevo API error " + response.statusCode() + ": " + response.body());
            }
            log.info("Email sent via Brevo to {} — status {}", toEmail, response.statusCode());
        } catch (Exception e) {
            log.error("Brevo HTTP email failed to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}