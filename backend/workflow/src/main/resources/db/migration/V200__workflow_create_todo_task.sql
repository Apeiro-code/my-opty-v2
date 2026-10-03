-- Client task board
-- Band: V200-V299
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- `order_id` is what makes a task an order-processing queue entry rather than a
-- to-do note. It is nullable because most tasks are not about an order.

CREATE TABLE todo_task (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(200) NOT NULL,
    description  TEXT         NULL,
    client_id    BIGINT       NOT NULL,
    order_id     BIGINT       NULL,
    status       VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    priority     VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    due_date     DATE         NULL,
    completed_at DATETIME(6)  NULL,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_todo_task_client (client_id),
    KEY idx_todo_task_status (status),
    KEY idx_todo_task_priority (priority),
    KEY idx_todo_task_due_date (due_date),
    KEY idx_todo_task_order (order_id),
    CONSTRAINT fk_todo_task_client
        FOREIGN KEY (client_id) REFERENCES app_user (id),
    CONSTRAINT fk_todo_task_order
        FOREIGN KEY (order_id) REFERENCES progressive_order (id) ON DELETE SET NULL,
    CONSTRAINT chk_todo_task_status
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'DONE')),
    CONSTRAINT chk_todo_task_priority
        CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
