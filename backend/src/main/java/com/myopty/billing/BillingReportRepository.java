package com.myopty.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingReportRepository extends JpaRepository<BillingReport, Integer> {
}