package com.myopty.workflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportSalesMonthlyRequest {
    private Integer clientId;
    private Integer month;
    private Integer year;

    // Default constructor
    public ReportSalesMonthlyRequest() {}

    // Getters and Setters
    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}