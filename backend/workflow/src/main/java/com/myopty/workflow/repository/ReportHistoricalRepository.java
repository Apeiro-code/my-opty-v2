package com.myopty.workflow.repository;

import com.myopty.workflow.model.ReportHistorical;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportHistoricalRepository extends ListCrudRepository<ReportHistorical, Integer> {

    @Query("SELECT * FROM report_historical WHERE client_id = :clientId AND month = :month AND year = :year")
    ReportHistorical findByClientIdAndMonthAndYear(Integer clientId, Integer month, Integer year);

    @Query("SELECT * FROM report_historical WHERE client_id = :clientId AND metric_type = :metricType ORDER BY recorded_at DESC")
    List<ReportHistorical> findByClientIdAndMetricTypeOrderByRecordedAtDesc(Integer clientId, String metricType);

    @Query("SELECT * FROM report_historical WHERE client_id = :clientId ORDER BY recorded_at DESC")
    List<ReportHistorical> findAllByClientIdOrderByRecordedAtDesc(Integer clientId);
}