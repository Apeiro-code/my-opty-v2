package com.myopty.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BillingReportService {

    @Autowired
    private BillingReportRepository billingReportRepository;

    public List<BillingReport> getAllReports() {
        return billingReportRepository.findAll();
    }

    public BillingReport saveReport(BillingReport billingReport) {
        return billingReportRepository.save(billingReport);
    }
}