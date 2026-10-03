-- Customer Q&A and the public FAQ
-- Band: V1 (nested version 1_2)
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- Unanswered questions are the client's work queue, so `is_faq` stays FALSE until
-- an answer is posted. The index on (is_faq, answered_at) is what makes the public
-- FAQ read cheap while the table also holds every open question.

CREATE TABLE question (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    customer_id    BIGINT      NOT NULL,
    question_text  TEXT        NOT NULL,
    answer_text    TEXT        NULL,
    is_faq         BOOLEAN     NOT NULL DEFAULT FALSE,
    asked_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    answered_at    DATETIME(6) NULL,
    answered_by    BIGINT      NULL,
    created_at     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_question_customer (customer_id),
    KEY idx_question_faq (is_faq, answered_at),
    KEY idx_question_answered_by (answered_by),
    CONSTRAINT fk_question_customer
        FOREIGN KEY (customer_id) REFERENCES app_user (id),
    CONSTRAINT fk_question_answered_by
        FOREIGN KEY (answered_by) REFERENCES app_user (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
