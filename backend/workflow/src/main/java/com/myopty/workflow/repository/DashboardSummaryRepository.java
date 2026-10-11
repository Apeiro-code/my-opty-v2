package com.myopty.workflow.repository;

import com.myopty.workflow.model.DashboardSummary;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DashboardSummaryRepository extends ListCrudRepository<DashboardSummary, Integer> {

    @Query("SELECT * FROM DASHBOARD_SUMMARY WHERE client_id = :clientId AND date = :date")
    DashboardSummary findByClientIdAndDate(Integer clientId, LocalDate date);

    @Query("SELECT * FROM DASHBOARD_SUMMARY WHERE client_id = :clientId ORDER BY date DESC")
    List<DashboardSummary> findAllByClientIdOrderByDateDesc(Integer clientId);

    @Query("SELECT * FROM DASHBOARD_SUMMARY WHERE date = :date ORDER BY total_sales DESC")
    List<DashboardSummary> findTopByDate(LocalDate date);
}