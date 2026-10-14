package com.myopty.shared.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "test", "default"})
@Primary
public class MockEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(MockEmailService.class);

    @Override
    public void sendVerificationEmail(String to, String code) {
        log.info("[MOCK EMAIL] Sending verification email to {} with code {}", to, code);
    }
}
