package com.myopty.workflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("report_inventory_monthly")
public class ReportInventoryMonthly {

    @Id
    private Integer reportId;

    private Integer clientId;

    private Integer month;

    private Integer year;

    private Integer stockAdded;

    private Integer stockSold;

    private Integer currentStock;

    private java.time.LocalDate createdAt;

    private java.time.LocalDate updatedAt;

    public ReportInventoryMonthly() {
        this.stockAdded = 0;
        this.stockSold = 0;
        this.currentStock = 0;
        this.createdAt = java.time.LocalDate.now();
        this.updatedAt = java.time.LocalDate.now();
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getStockAdded() {
        return stockAdded;
    }

    public void setStockAdded(Integer stockAdded) {
        this.stockAdded = stockAdded;
    }

    public Integer getStockSold() {
        return stockSold;
    }

    public void setStockSold(Integer stockSold) {
        this.stockSold = stockSold;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public java.time.LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }
}
