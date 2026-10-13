package com.myopty.shared.question.service;

import com.myopty.shared.question.dto.AnswerQuestionRequest;
import com.myopty.shared.question.dto.CreateQuestionRequest;
import com.myopty.shared.question.dto.FaqResponse;
import com.myopty.shared.question.dto.QuestionResponse;
import com.myopty.shared.question.dto.ShopQuestionResponse;
import com.myopty.shared.question.exception.AlreadyAnsweredException;
import com.myopty.shared.question.exception.QuestionNotFoundException;
import com.myopty.shared.question.model.Question;
import com.myopty.shared.question.repository.QuestionRepository;
import com.myopty.shared.user.AppUser;
import com.myopty.shared.user.AppUserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Both sides of the Q&amp;A: the customer asks and reads their own, the shop
 * answers, and the public FAQ exposes what the shop chose to publish.
 *
 * <p>Identity is always taken from the caller's session, never from a request
 * body: the customer's id on {@link #ask} and the shop's id on {@link #answer}
 * are passed in by the controller, so neither side can file under someone else.
 * A new question is never born a FAQ — {@code is_faq} stays false until the
 * (separate) FAQ-marking story promotes it, which is why the customer read and
 * the FAQ read are separate queries rather than one filtered list.
 *
 * <p>Answering commits the answer first and emails the customer afterwards, in an
 * after-commit callback. The answer is the source of truth: a mail server that is
 * down must not undo the shop's reply, so a failed send is logged and the answer
 * stands, the same posture {@code order}'s notifications take.
 */
@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questions;
    private final AppUserRepository users;
    private final QuestionNotifier notifier;

    public QuestionService(QuestionRepository questions, AppUserRepository users, QuestionNotifier notifier) {
        this.questions = questions;
        this.users = users;
        this.notifier = notifier;
    }

    @Transactional
    public QuestionResponse ask(Long customerId, CreateQuestionRequest request) {
        Question question = new Question();
        question.setCustomerId(customerId);
        question.setQuestionText(request.questionText().trim());
        question.setFaq(false);
        return QuestionResponse.from(questions.save(question));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listForCustomer(Long customerId) {
        return questions.findForCustomer(customerId).stream()
                .map(QuestionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FaqResponse> listFaq() {
        return questions.findAnsweredFaqs().stream().map(FaqResponse::from).toList();
    }

    /** The shop's answering queue: every open question, longest-waiting first. */
    @Transactional(readOnly = true)
    public List<ShopQuestionResponse> listUnanswered() {
        return questions.findUnanswered().stream().map(this::toShopResponse).toList();
    }

    /**
     * Records the shop's answer and tells the customer. Refuses a question that
     * does not exist ({@code 404}) or that already has an answer ({@code 409}) —
     * answering twice would silently rewrite what the customer was already told.
     */
    @Transactional
    public QuestionResponse answer(long questionId, long clientId, AnswerQuestionRequest request) {
        Question question = questions.findById(questionId).orElseThrow(() -> new QuestionNotFoundException(questionId));
        if (question.getAnswerText() != null) {
            throw new AlreadyAnsweredException(questionId);
        }

        question.setAnswerText(request.answerText().trim());
        question.setAnsweredAt(LocalDateTime.now());
        question.setAnsweredBy(clientId);
        Question answered = questions.save(question);

        notifyCustomerAfterCommit(answered);
        return QuestionResponse.from(answered);
    }

    private ShopQuestionResponse toShopResponse(Question question) {
        return users.findById(question.getCustomerId())
                .map(user -> ShopQuestionResponse.from(question, user.getFullName(), user.getEmail()))
                .orElseGet(() -> ShopQuestionResponse.from(question, null, null));
    }

    /**
     * Emails the answer once the transaction has committed, so a delivery failure
     * cannot roll back the answer. The recipient is resolved from the question's
     * customer id, the only identity the row carries.
     */
    private void notifyCustomerAfterCommit(Question question) {
        String email = users.findById(question.getCustomerId())
                .map(AppUser::getEmail)
                .filter(address -> !address.isBlank())
                .orElse(null);
        if (email == null) {
            log.warn(
                    "No email address for customer {}; answer to question {} was saved but not emailed.",
                    question.getCustomerId(),
                    question.getId());
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            sendQuietly(question, email);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendQuietly(question, email);
            }
        });
    }

    private void sendQuietly(Question question, String email) {
        try {
            notifier.notifyAnswered(question, email);
        } catch (RuntimeException exception) {
            log.warn("Could not email answer to question {}: {}", question.getId(), exception.getMessage());
        }
    }
}
