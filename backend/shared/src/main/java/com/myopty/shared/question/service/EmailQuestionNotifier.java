package com.myopty.shared.question.service;

import com.myopty.shared.question.model.Question;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Emails a customer their answer over the {@code MAIL_*} SMTP contract.
 *
 * <p>The question and answer are rendered into a short body from the stored rows,
 * because the answer is the message and there is no separate notification record
 * to keep in step — the {@code question} row itself is what was said. Delivery
 * failures surface as a {@code MailException}; the caller decides whether that is
 * fatal, since only it knows the answer is already committed.
 */
public class EmailQuestionNotifier implements QuestionNotifier {

    /** Constant because the answered question is already in the body. */
    static final String SUBJECT = "Your MyOpty question has been answered";

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailQuestionNotifier(JavaMailSender mailSender, String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void notifyAnswered(Question question, String recipientEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject(SUBJECT);
        message.setText("Hello,\n\nThe shop has answered your question:\n\n\""
                + question.getQuestionText() + "\"\n\nAnswer:\n" + question.getAnswerText()
                + "\n\nYou can read it any time at Flanet Opticals, Narammala.");
        mailSender.send(message);
    }
}
