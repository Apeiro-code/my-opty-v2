package com.myopty.shared.question.exception;

/**
 * The shop tried to answer a question that already has an answer.
 *
 * <p>Answering is a one-way move in this story — a question is open or it is
 * answered. A second answer would silently rewrite what the customer was already
 * emailed, so it is refused as a durable state conflict ({@code 409}) rather than
 * accepted as an edit.
 */
public class AlreadyAnsweredException extends RuntimeException {

    public AlreadyAnsweredException(long id) {
        super("Question " + id + " has already been answered.");
    }
}
