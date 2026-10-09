package com.myopty.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class BillingReportController {

    @Autowired
    private BillingReportService billingReportService;


    @GetMapping
    public List<BillingReport> getAllReports() {
        return billingReportService.getAllReports();
    }


    @PostMapping("/generate")
    public BillingReport generateReport(@RequestParam String month) {
        return billingReportService.generateMonthlyReport(month);
    }
}