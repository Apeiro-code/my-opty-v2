package com.myopty.workflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DashboardSummaryResponse {
    private Integer summaryId;
    private Integer clientId;
    private LocalDate date;
    private Double totalSales;
    private Integer totalOrders;
    private Integer lowStockItems;
    private Integer pendingTasks;
    private LocalDate createdAt;
    private LocalDateTime createdAtTime;

    // Default constructor
    public DashboardSummaryResponse() {}

    // Constructor from Entity
    public DashboardSummaryResponse(Integer summaryId, Integer clientId, LocalDate date,
                                   Double totalSales, Integer totalOrders, Integer lowStockItems,
                                   Integer pendingTasks, LocalDate createdAt, LocalDateTime createdAtTime) {
        this.summaryId = summaryId;
        this.clientId = clientId;
        this.date = date;
        this.totalSales = totalSales;
        this.totalOrders = totalOrders;
        this.lowStockItems = lowStockItems;
        this.pendingTasks = pendingTasks;
        this.createdAt = createdAt;
        this.createdAtTime = createdAtTime;
    }

    // Getters and Setters
    public Integer getSummaryId() { return summaryId; }
    public void setSummaryId(Integer summaryId) { this.summaryId = summaryId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getTotalSales() { return totalSales; }
    public void setTotalSales(Double totalSales) { this.totalSales = totalSales; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public Integer getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(Integer lowStockItems) { this.lowStockItems = lowStockItems; }

    public Integer getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(Integer pendingTasks) { this.pendingTasks = pendingTasks; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCreatedAtTime() { return createdAtTime; }
    public void setCreatedAtTime(LocalDateTime createdAtTime) { this.createdAtTime = createdAtTime; }
}