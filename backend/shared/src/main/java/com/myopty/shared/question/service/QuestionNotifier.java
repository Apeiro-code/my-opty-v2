package com.myopty.shared.question.service;

import com.myopty.shared.question.model.Question;

/**
 * Tells a customer their question has been answered.
 *
 * <p>{@link QuestionService} depends on this one method rather than on the mail
 * details, so whether the telling is emailed, queued or mocked is decided by the
 * bean wiring and not by the answering logic. It keeps the seam open for a second
 * channel without changing the service.
 */
public interface QuestionNotifier {

    /** Sends the answer to the customer who asked the question. */
    void notifyAnswered(Question question, String recipientEmail);
}
