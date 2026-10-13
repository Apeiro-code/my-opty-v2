package com.myopty.workflow.service;

import com.myopty.workflow.dto.*;
import com.myopty.workflow.model.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final com.myopty.workflow.repository.ReportSalesMonthlyRepository salesRepo;
    private final com.myopty.workflow.repository.ReportInventoryMonthlyRepository inventoryRepo;
    private final com.myopty.workflow.repository.ReportHistoricalRepository historicalRepo;
    private final com.myopty.workflow.repository.DashboardSummaryRepository dashboardRepo;

    public ReportService(
            com.myopty.workflow.repository.ReportSalesMonthlyRepository salesRepo,
            com.myopty.workflow.repository.ReportInventoryMonthlyRepository inventoryRepo,
            com.myopty.workflow.repository.ReportHistoricalRepository historicalRepo,
            com.myopty.workflow.repository.DashboardSummaryRepository dashboardRepo) {
        this.salesRepo = salesRepo;
        this.inventoryRepo = inventoryRepo;
        this.historicalRepo = historicalRepo;
        this.dashboardRepo = dashboardRepo;
    }

    // ===== Monthly Sales Report =====

    public ReportSalesMonthlyResponse generateMonthlySalesReport(ReportSalesMonthlyRequest request) {
        // Check if report already exists for this client/month/year
        ReportSalesMonthly existing =
                salesRepo.findByClientIdAndMonthAndYear(request.getClientId(), request.getMonth(), request.getYear());

        if (existing == null) {
            existing = new ReportSalesMonthly();
            existing.setClientId(request.getClientId());
            existing.setMonth(request.getMonth());
            existing.setYear(request.getYear());
            existing.setTotalOrders(0);
            existing.setTotalRevenue(0.0);
            existing = salesRepo.save(existing);
        }

        return convertToResponse(existing);
    }

    public List<ReportHistoricalResponse> getMonthlySalesHistory(ReportHistoricalRequest request) {
        List<ReportHistorical> history = historicalRepo.findAllByClientIdOrderByRecordedAtDesc(request.getClientId());
        List<ReportHistoricalResponse> responses = new ArrayList<>();
        for (ReportHistorical h : history) {
            responses.add(new ReportHistoricalResponse(
                    h.getHistoricalId(),
                    h.getClientId(),
                    h.getMonth(),
                    h.getYear(),
                    h.getMetricType(),
                    h.getMetricValue(),
                    h.getRecordedAt(),
                    null));
        }
        return responses;
    }

    // ===== Monthly Inventory Report =====

    public ReportInventoryMonthlyResponse generateMonthlyInventoryReport(ReportInventoryMonthlyRequest request) {
        ReportInventoryMonthly existing = inventoryRepo.findByClientIdAndMonthAndYear(
                request.getClientId(), request.getMonth(), request.getYear());

        if (existing == null) {
            existing = new ReportInventoryMonthly();
            existing.setClientId(request.getClientId());
            existing.setMonth(request.getMonth());
            existing.setYear(request.getYear());
            existing.setStockAdded(0);
            existing.setStockSold(0);
            existing.setCurrentStock(0);
            existing = inventoryRepo.save(existing);
        }

        return convertInventoryResponse(existing);
    }

    // ===== Historical Reports ( unified return type) =====

    public List<ReportHistoricalResponse> getInventoryReportHistory(ReportHistoricalRequest request) {
        List<ReportHistorical> history = historicalRepo.findAllByClientIdOrderByRecordedAtDesc(request.getClientId());
        List<ReportHistoricalResponse> historyResponses = new ArrayList<>();
        for (ReportHistorical h : history) {
            historyResponses.add(new ReportHistoricalResponse(
                    h.getHistoricalId(),
                    h.getClientId(),
                    h.getMonth(),
                    h.getYear(),
                    h.getMetricType(),
                    h.getMetricValue(),
                    h.getRecordedAt(),
                    null));
        }
        return historyResponses;
    }

    // ===== Historical Reports =====

    public List<ReportHistoricalResponse> getHistoricalReports(ReportHistoricalRequest request) {
        List<ReportHistorical> history = historicalRepo.findAllByClientIdOrderByRecordedAtDesc(request.getClientId());
        List<ReportHistoricalResponse> responses = new ArrayList<>();
        for (ReportHistorical h : history) {
            responses.add(new ReportHistoricalResponse(
                    h.getHistoricalId(),
                    h.getClientId(),
                    h.getMonth(),
                    h.getYear(),
                    h.getMetricType(),
                    h.getMetricValue(),
                    h.getRecordedAt(),
                    null));
        }
        return responses;
    }

    // ===== Dashboard Summary =====

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public DashboardSummaryResponse getDailyDashboardSummary(Integer clientId, LocalDate date) {
        DashboardSummary existing = dashboardRepo.findByClientIdAndDate(clientId, date);

        if (existing == null) {
            // Calculate summary metrics
            Double totalSales = 0.0;
            Integer totalOrders = 0;
            Integer lowStockItems = 0;
            Integer pendingTasks = 0;

            // In a real implementation, these would query the actual data
            // For now, set default values
            existing = new DashboardSummary();
            existing.setClientId(clientId);
            existing.setDate(date);
            existing.setTotalSales(totalSales);
            existing.setTotalOrders(totalOrders);
            existing.setLowStockItems(lowStockItems);
            existing.setPendingTasks(pendingTasks);
            existing = dashboardRepo.save(existing);
        }

        return convertToResponse(existing);
    }

    // ===== Export Helper Methods =====

    // Convert entity to response for sales monthly
    private ReportSalesMonthlyResponse convertToResponse(ReportSalesMonthly entity) {
        return new ReportSalesMonthlyResponse(
                entity.getReportId(),
                entity.getClientId(),
                entity.getMonth(),
                entity.getYear(),
                entity.getTotalOrders(),
                entity.getTotalRevenue(),
                entity.getCreatedAt(),
                null);
    }

    // Convert entity to response for inventory monthly
    private ReportInventoryMonthlyResponse convertInventoryResponse(ReportInventoryMonthly entity) {
        return new ReportInventoryMonthlyResponse(
                entity.getReportId(),
                entity.getClientId(),
                entity.getMonth(),
                entity.getYear(),
                entity.getStockAdded(),
                entity.getStockSold(),
                entity.getCurrentStock(),
                entity.getCreatedAt(),
                null);
    }

    // Convert entity to response for dashboard summary
    private DashboardSummaryResponse convertToResponse(DashboardSummary entity) {
        return new DashboardSummaryResponse(
                entity.getSummaryId(),
                entity.getClientId(),
                entity.getDate(),
                entity.getTotalSales(),
                entity.getTotalOrders(),
                entity.getLowStockItems(),
                entity.getPendingTasks(),
                entity.getCreatedAt(),
                null);
    }
}
