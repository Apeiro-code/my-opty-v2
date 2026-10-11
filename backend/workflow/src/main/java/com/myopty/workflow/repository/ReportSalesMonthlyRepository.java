package com.myopty.workflow.repository;

import com.myopty.workflow.model.ReportSalesMonthly;
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