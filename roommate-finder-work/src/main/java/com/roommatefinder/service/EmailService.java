package com.roommatefinder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
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
        mailSender.send(message);
    }

    @Async
    public void sendMatchRequestEmail(String toEmail, String receiverName, String senderName, int compatibilityScore) {
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
    }

    @Async
    public void sendMatchAcceptedEmail(String toEmail, String senderName, String receiverName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your roommate request was accepted! 🎉");
        message.setText(
            "Hi " + senderName + ",\n\n" +
            "Great news! " + receiverName + " accepted your roommate request.\n\n" +
            "Login to view their contact details and get in touch: " + frontendUrl + "/dashboard\n\n" +
            "— RoommateFinder Team"
        );
        mailSender.send(message);
    }
    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Reset your RoommateFinder password");
        message.setText(
            "Hi " + name + ",\n\n" +
            "We received a request to reset your password.\n\n" +
            "Click the link below to choose a new password (expires in 1 hour):\n" +
            frontendUrl + "/reset-password?token=" + resetToken + "\n\n" +
            "If you did not request this, you can safely ignore this email.\n\n" +
            "— RoommateFinder Team"
        );
        mailSender.send(message);
    }
}
