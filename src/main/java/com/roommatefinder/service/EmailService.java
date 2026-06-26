package com.roommatefinder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // NO @Async here — must succeed or register fails with a visible error
    public void sendVerificationEmail(String toEmail, String name, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify your RoommateFinder account");
        message.setText(
                "Hi " + name + ",\n\n" +
                        "Welcome to RoommateFinder! Please verify your email to get started.\n\n" +
                        "Click here to verify: " + frontendUrl + "/verify-email?token=" + token + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "— RoommateFinder Team"
        );
        try {
            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Verification email FAILED to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }

    @Async
    public void sendMatchRequestEmail(String toEmail, String receiverName, String senderName, int compatibilityScore) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("You have a new roommate request! 🏠");
            message.setText(
                    "Hi " + receiverName + ",\n\n" +
                            senderName + " wants to connect with you as a roommate!\n\n" +
                            "Compatibility Score: " + compatibilityScore + "%\n\n" +
                            "Login to view their profile and respond: " + frontendUrl + "/dashboard\n\n" +
                            "— RoommateFinder Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send match request email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendMatchAcceptedEmail(String toEmail, String senderName, String receiverName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your roommate request was accepted! 🎉");
            message.setText(
                    "Hi " + senderName + ",\n\n" +
                            "Great news! " + receiverName + " accepted your roommate request.\n\n" +
                            "Login to view their contact details: " + frontendUrl + "/dashboard\n\n" +
                            "— RoommateFinder Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send match accepted email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset your RoommateFinder password");
            message.setText(
                    "Hi " + name + ",\n\n" +
                            "Click the link below to reset your password (expires in 1 hour):\n" +
                            frontendUrl + "/reset-password?token=" + resetToken + "\n\n" +
                            "If you did not request this, ignore this email.\n\n" +
                            "— RoommateFinder Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}
