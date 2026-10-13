package com.myopty.workflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("report_sales_monthly")
public class ReportSalesMonthly {

    @Id
    private Integer reportId;

    private Integer clientId;

    private Integer month;

    private Integer year;

    private Integer totalOrders;

    private Double totalRevenue;

    private java.time.LocalDate createdAt;

    private java.time.LocalDate updatedAt;

    public ReportSalesMonthly() {
        this.totalOrders = 0;
        this.totalRevenue = 0.0;
        this.createdAt = java.time.LocalDate.now();
        this.updatedAt = java.time.LocalDate.now();
    }

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

    public java.time.LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDate createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDate updatedAt) { this.updatedAt = updatedAt; }
}