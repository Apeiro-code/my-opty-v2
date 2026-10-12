package com.myopty.workflow.controller;

import com.myopty.workflow.dto.*;
import com.myopty.workflow.model.*;
import com.myopty.workflow.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

// ===== Monthly Sales Report =====

    @GetMapping("/sales/monthly")
    public ReportSalesMonthlyResponse getMonthlySalesReport(
            @RequestParam Integer clientId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        ReportSalesMonthlyRequest request = new ReportSalesMonthlyRequest();
        request.setClientId(clientId);
        if (month != null) request.setMonth(month);
        if (year != null) request.setYear(year);
        return service.generateMonthlySalesReport(request);
    }

    @PostMapping("/sales/monthly")
    public ReportSalesMonthlyResponse generateMonthlySalesReport(
            @RequestBody ReportSalesMonthlyRequest request) {
        return service.generateMonthlySalesReport(request);
    }

    // ===== Monthly Inventory Report =====

    @GetMapping("/inventory/monthly")
    public ReportInventoryMonthlyResponse getMonthlyInventoryReport(
            @RequestParam Integer clientId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        ReportInventoryMonthlyRequest request = new ReportInventoryMonthlyRequest();
        request.setClientId(clientId);
        if (month != null) request.setMonth(month);
        if (year != null) request.setYear(year);
        return service.generateMonthlyInventoryReport(request);
    }

    @PostMapping("/inventory/monthly")
    public ReportInventoryMonthlyResponse generateMonthlyInventoryReport(
            @RequestBody ReportInventoryMonthlyRequest request) {
        return service.generateMonthlyInventoryReport(request);
    }

    // ===== Historical Reports =====

    @GetMapping("/historical")
    public List<ReportHistoricalResponse> getHistoricalReports(
            @RequestParam Integer clientId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String metricType) {
        ReportHistoricalRequest request = new ReportHistoricalRequest();
        request.setClientId(clientId);
        if (month != null) request.setMonth(month);
        if (year != null) request.setYear(year);
        if (metricType != null) request.setMetricType(metricType);
        return service.getHistoricalReports(request);
    }

    // ===== Dashboard Summary =====

    @GetMapping("/dashboard/summary")
    public DashboardSummaryResponse getDailyDashboardSummary(
            @RequestParam Integer clientId,
            @RequestParam(required = false) String date) {
        LocalDate localDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return service.getDailyDashboardSummary(clientId, localDate);
    }

    // ===== Export Functionality =====

    @GetMapping("/export/sales/pdf")
    public byte[] exportSalesPdf(
            @RequestParam Integer clientId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        // In a real implementation, this would generate a PDF report
        // For now, return empty bytes with appropriate headers
        return new byte[0];
    }

    @GetMapping("/export/sales/excel")
    public byte[] exportSalesExcel(
            @RequestParam Integer clientId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        // In a real implementation, this would generate an Excel report
        // For now, return empty bytes with appropriate headers
        return new byte[0];
    }

    @GetMapping("/export/inventory/pdf")
    public byte[] exportInventoryPdf(
            @RequestParam Integer clientId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        // In a real implementation, this would generate a PDF inventory report
        return new byte[0];
    }

    @GetMapping("/export/inventory/excel")
    public byte[] exportInventoryExcel(
            @RequestParam Integer clientId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        // In a real implementation, this would generate an Excel inventory report
        return new byte[0];
    }

    @GetMapping("/export/dashboard/pdf")
    public byte[] exportDashboardPdf(
            @RequestParam Integer clientId,
            @RequestParam(required = false) String date) {
        LocalDate localDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return new byte[0];
    }

    @GetMapping("/export/dashboard/excel")
    public byte[] exportDashboardExcel(
            @RequestParam Integer clientId,
            @RequestParam(required = false) String date) {
        LocalDate localDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return new byte[0];
    }
}