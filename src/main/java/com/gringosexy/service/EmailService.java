package com.gringosexy.service;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String fullName, String token);

    void sendPasswordResetEmail(String toEmail, String fullName, String token);

    void sendAccountStatusNotification(String toEmail, String fullName, String newStatusDescription);
}
