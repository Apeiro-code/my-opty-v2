package com.myopty.workflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportInventoryMonthlyResponse {
    private Integer reportId;
    private Integer clientId;
    private Integer month;
    private Integer year;
    private Integer stockAdded;
    private Integer stockSold;
    private Integer currentStock;
    private LocalDate createdAt;
    private LocalDateTime createdAtTime;

    // Default constructor
    public ReportInventoryMonthlyResponse() {}

    // Constructor from Entity
    public ReportInventoryMonthlyResponse(Integer reportId, Integer clientId, Integer month, Integer year,
                                          Integer stockAdded, Integer stockSold, Integer currentStock, LocalDate createdAt, LocalDateTime createdAtTime) {
        this.reportId = reportId;
        this.clientId = clientId;
        this.month = month;
        this.year = year;
        this.stockAdded = stockAdded;
        this.stockSold = stockSold;
        this.currentStock = currentStock;
        this.createdAt = createdAt;
        this.createdAtTime = createdAtTime;
    }

    // Getters and Setters
    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getStockAdded() { return stockAdded; }
    public void setStockAdded(Integer stockAdded) { this.stockAdded = stockAdded; }

    public Integer getStockSold() { return stockSold; }
    public void setStockSold(Integer stockSold) { this.stockSold = stockSold; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCreatedAtTime() { return createdAtTime; }
    public void setCreatedAtTime(LocalDateTime createdAtTime) { this.createdAtTime = createdAtTime; }
}