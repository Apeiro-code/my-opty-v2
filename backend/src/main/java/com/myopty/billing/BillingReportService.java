package com.myopty.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillingReportService {

    @Autowired
    private BillingReportRepository billingReportRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;


    public List<BillingReport> getAllReports() {
        return billingReportRepository.findAll();
    }


    public BillingReport generateMonthlyReport(String month) {

        Double totalRevenue = invoiceRepository.findAll().stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        BillingReport report = new BillingReport();
        report.setMonth(month);
        report.setTotalRevenue(totalRevenue);

        return billingReportRepository.save(report);
    }
}