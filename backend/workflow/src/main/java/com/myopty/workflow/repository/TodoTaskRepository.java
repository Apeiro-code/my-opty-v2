package com.myopty.workflow.repository;

import com.myopty.workflow.model.TodoTask;
import org.springframework.data.jdbc.repository.support.SimpleJdbcRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class TodoTaskRepository extends SimpleJdbcRepository<TodoTask, Integer> {

    @PersistenceContext
    private EntityManager entityManager;

    public TodoTaskRepository() {
        super(TodoTask.class);
    }

    public Optional<TodoTask> findByClientId(Integer clientId) {
        return Optional.ofNullable(
            entityManager.createQuery(
                "SELECT t FROM TodoTask t WHERE t.clientId = :clientId", TodoTask.class)
                .setParameter("clientId", clientId)
                .getSingleResult()
        );
    }

    public java.util.List<TodoTask> findAllByClientId(Integer clientId) {
        return entityManager.createQuery(
            "SELECT t FROM TodoTask t WHERE t.clientId = :clientId ORDER BY t.taskId DESC", TodoTask.class)
            .setParameter("clientId", clientId)
            .getResultList();
    }
}