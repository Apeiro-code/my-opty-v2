package com.myopty.workflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportSalesMonthlyResponse {
    private Integer reportId;
    private Integer clientId;
    private Integer month;
    private Integer year;
    private Integer totalOrders;
    private Double totalRevenue;
    private LocalDate createdAt;
    private LocalDateTime createdAtTime;

    // Default constructor
    public ReportSalesMonthlyResponse() {}

    // Constructor from Entity
    public ReportSalesMonthlyResponse(Integer reportId, Integer clientId, Integer month, Integer year,
                                      Integer totalOrders, Double totalRevenue, LocalDate createdAt, LocalDateTime createdAtTime) {
        this.reportId = reportId;
        this.clientId = clientId;
        this.month = month;
        this.year = year;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
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

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCreatedAtTime() { return createdAtTime; }
    public void setCreatedAtTime(LocalDateTime createdAtTime) { this.createdAtTime = createdAtTime; }
}