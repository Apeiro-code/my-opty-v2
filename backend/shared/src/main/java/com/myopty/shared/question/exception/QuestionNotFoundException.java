package com.myopty.shared.question.exception;

/**
 * The shop tried to answer a question that does not exist.
 *
 * <p>A missing resource, so the controller answers {@code 404}, distinct from the
 * {@code 409} an already-answered question returns.
 */
public class QuestionNotFoundException extends RuntimeException {

    public QuestionNotFoundException(long id) {
        super("No question with id " + id + ".");
    }
}
