package com.myopty.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/billing-reports")
public class BillingReportController {

    @Autowired
    private BillingReportService billingReportService;

    @GetMapping
    public List<BillingReport> getAllReports() {
        return billingReportService.getAllReports();
    }

    @PostMapping
    public BillingReport createReport(@RequestBody BillingReport billingReport) {
        return billingReportService.saveReport(billingReport);
    }
}