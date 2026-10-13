package com.myopty.shared.email;

public interface EmailService {
    void sendVerificationEmail(String to, String code);
}
