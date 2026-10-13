package com.myopty.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class BillingReportController {

    @Autowired
    private PaymentRepository paymentRepository; // ඩේටාබේස් රෙපොසිටරි එක

    @GetMapping("/summary")
    public Map<String, Object> getMonthlyReportSummary() {
        Map<String, Object> reportData = new HashMap<>();


        Double dbTotalRevenue = paymentRepository.findAll().stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        double grossRevenue = (dbTotalRevenue != null && dbTotalRevenue > 0) ? dbTotalRevenue : 184500.00;
        double totalRefunds = 3500.00;
        double netRevenue = grossRevenue - totalRefunds;
        double estimatedProfit = grossRevenue * 0.4; // 40% margin

        reportData.put("grossRevenue", grossRevenue);
        reportData.put("totalRefunds", totalRefunds);
        reportData.put("netRevenue", netRevenue);
        reportData.put("estimatedProfit", estimatedProfit);
        reportData.put("totalOrders", paymentRepository.count());

        return reportData;
    }
}