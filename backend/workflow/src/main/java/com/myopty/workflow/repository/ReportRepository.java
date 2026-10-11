package com.myopty.workflow.repository;

import com.myopty.workflow.model.ReportHistorical;
import com.myopty.workflow.model.ReportSalesMonthly;
import com.myopty.workflow.model.ReportInventoryMonthly;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportSalesMonthlyRepository extends ListCrudRepository<ReportSalesMonthly, Integer> {

    @Query("SELECT * FROM REPORT_SALES_MONTHLY WHERE client_id = :clientId AND month = :month AND year = :year")
    ReportSalesMonthly findByClientIdAndMonthAndYear(Integer clientId, Integer month, Integer year);

    @Query("SELECT * FROM REPORT_SALES_MONTHLY WHERE client_id = :clientId ORDER BY year DESC, month DESC")
    List<ReportSalesMonthly> findAllByClientIdOrderByYearDescMonthDesc(Integer clientId);
}

@Repository
public interface ReportInventoryMonthlyRepository extends ListCrudRepository<ReportInventoryMonthly, Integer> {

    @Query("SELECT * FROM REPORT_INVENTORY_MONTHLY WHERE client_id = :clientId AND month = :month AND year = :year")
    ReportInventoryMonthly findByClientIdAndMonthAndYear(Integer clientId, Integer month, Integer year);

    @Query("SELECT * FROM REPORT_INVENTORY_MONTHLY WHERE client_id = :clientId ORDER BY year DESC, month DESC")
    List<ReportInventoryMonthly> findAllByClientIdOrderByYearDescMonthDesc(Integer clientId);
}

@Repository
public interface ReportHistoricalRepository extends ListCrudRepository<ReportHistorical, Integer> {

    @Query("SELECT * FROM REPORT_HISTORICAL WHERE client_id = :clientId AND month = :month AND year = :year")
    ReportHistorical findByClientIdAndMonthAndYear(Integer clientId, Integer month, Integer year);

    @Query("SELECT * FROM REPORT_HISTORICAL WHERE client_id = :clientId AND metric_type = :metricType ORDER BY recorded_at DESC")
    List<ReportHistorical> findByClientIdAndMetricTypeOrderByRecordedAtDesc(Integer clientId, String metricType);

    @Query("SELECT * FROM REPORT_HISTORICAL WHERE client_id = :clientId ORDER BY recorded_at DESC")
    List<ReportHistorical> findAllByClientIdOrderByRecordedAtDesc(Integer clientId);
}

@Repository
public interface DashboardSummaryRepository extends ListCrudRepository<DashboardSummary, Integer> {

    @Query("SELECT * FROM DASHBOARD_SUMMARY WHERE client_id = :clientId AND date = :date")
    DashboardSummary findByClientIdAndDate(Integer clientId, java.time.LocalDate date);

    @Query("SELECT * FROM DASHBOARD_SUMMARY WHERE client_id = :clientId ORDER BY date DESC")
    List<DashboardSummary> findAllByClientIdOrderByDateDesc(Integer clientId);

    @Query("SELECT * FROM DASHBOARD_SUMMARY WHERE date = :date ORDER BY total_sales DESC")
    List<DashboardSummary> findTopByDate(java.time.LocalDate date);
}