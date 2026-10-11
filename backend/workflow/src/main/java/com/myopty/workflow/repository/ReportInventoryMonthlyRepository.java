package com.myopty.workflow.repository;

import com.myopty.workflow.model.ReportInventoryMonthly;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportInventoryMonthlyRepository extends ListCrudRepository<ReportInventoryMonthly, Integer> {

    @Query("SELECT * FROM REPORT_INVENTORY_MONTHLY WHERE client_id = :clientId AND month = :month AND year = :year")
    ReportInventoryMonthly findByClientIdAndMonthAndYear(Integer clientId, Integer month, Integer year);

    @Query("SELECT * FROM REPORT_INVENTORY_MONTHLY WHERE client_id = :clientId ORDER BY year DESC, month DESC")
    List<ReportInventoryMonthly> findAllByClientIdOrderByYearDescMonthDesc(Integer clientId);
}