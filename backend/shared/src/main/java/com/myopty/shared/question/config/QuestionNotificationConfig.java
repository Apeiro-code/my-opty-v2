package com.myopty.shared.question.config;

import com.myopty.shared.question.service.EmailQuestionNotifier;
import com.myopty.shared.question.service.QuestionNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Wires the Q&amp;A notification sender.
 *
 * <p>The {@link QuestionNotifier} bean is the seam a second channel replaces: the
 * service depends on the interface, so swapping SMTP for something else is this
 * one method. The visible From: address reuses {@code MAIL_FROM_ADDRESS}, the same
 * contract order notifications and low-stock alerts send from, so the shop
 * configures one sender rather than one per feature.
 */
@Configuration
public class QuestionNotificationConfig {

    @Bean
    QuestionNotifier questionNotifier(
            JavaMailSender mailSender, @Value("${MAIL_FROM_ADDRESS:no-reply@myopty.local}") String fromAddress) {
        return new EmailQuestionNotifier(mailSender, fromAddress);
    }
}
