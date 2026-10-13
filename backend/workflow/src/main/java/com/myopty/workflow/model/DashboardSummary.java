package com.myopty.workflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("DASHBOARD_SUMMARY")
public class DashboardSummary {

    @Id
    private Integer summaryId;

    private Integer clientId;

    private java.time.LocalDate date;

    private Double totalSales;

    private Integer totalOrders;

    private Integer lowStockItems;

    private Integer pendingTasks;

    private java.time.LocalDate createdAt;

    private java.time.LocalDate updatedAt;

    public DashboardSummary() {
        this.totalSales = 0.0;
        this.totalOrders = 0;
        this.lowStockItems = 0;
        this.pendingTasks = 0;
        this.createdAt = java.time.LocalDate.now();
        this.updatedAt = java.time.LocalDate.now();
    }

    public Integer getSummaryId() { return summaryId; }
    public void setSummaryId(Integer summaryId) { this.summaryId = summaryId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public java.time.LocalDate getDate() { return date; }
    public void setDate(java.time.LocalDate date) { this.date = date; }

    public Double getTotalSales() { return totalSales; }
    public void setTotalSales(Double totalSales) { this.totalSales = totalSales; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public Integer getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(Integer lowStockItems) { this.lowStockItems = lowStockItems; }

    public Integer getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(Integer pendingTasks) { this.pendingTasks = pendingTasks; }

    public java.time.LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDate createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDate updatedAt) { this.updatedAt = updatedAt; }
}