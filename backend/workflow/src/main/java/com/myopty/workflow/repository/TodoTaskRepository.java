package com.myopty.workflow.repository;

import com.myopty.workflow.model.TodoTask;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoTaskRepository extends ListCrudRepository<TodoTask, Integer> {

    @Query("SELECT * FROM TODO_TASK WHERE client_id = :clientId ORDER BY task_id DESC")
    List<TodoTask> findAllByClientId(Integer clientId);

    @Query("SELECT * FROM TODO_TASK WHERE client_id = :clientId AND status = 'PENDING' ORDER BY created_at ASC")
    List<TodoTask> findPendingTasksByClientId(Integer clientId);

    @Query("SELECT * FROM TODO_TASK WHERE client_id = :clientId AND due_date < CURRENT_DATE AND status != 'DONE'")
    List<TodoTask> findOverdueTasksByClientId(Integer clientId);
}