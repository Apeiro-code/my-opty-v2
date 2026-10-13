package com.myopty.workflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportHistoricalResponse {
    private Integer historicalId;
    private Integer clientId;
    private Integer month;
    private Integer year;
    private String metricType;
    private Double metricValue;
    private LocalDate recordedAt;
    private LocalDateTime recordedAtTime;

    // Default constructor
    public ReportHistoricalResponse() {}

    // Constructor from Entity
    public ReportHistoricalResponse(Integer historicalId, Integer clientId, Integer month, Integer year,
                                   String metricType, Double metricValue, LocalDate recordedAt, LocalDateTime recordedAtTime) {
        this.historicalId = historicalId;
        this.clientId = clientId;
        this.month = month;
        this.year = year;
        this.metricType = metricType;
        this.metricValue = metricValue;
        this.recordedAt = recordedAt;
        this.recordedAtTime = recordedAtTime;
    }

    // Getters and Setters
    public Integer getHistoricalId() { return historicalId; }
    public void setHistoricalId(Integer historicalId) { this.historicalId = historicalId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }

    public Double getMetricValue() { return metricValue; }
    public void setMetricValue(Double metricValue) { this.metricValue = metricValue; }

    public LocalDate getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDate recordedAt) { this.recordedAt = recordedAt; }

    public LocalDateTime getRecordedAtTime() { return recordedAtTime; }
    public void setRecordedAtTime(LocalDateTime recordedAtTime) { this.recordedAtTime = recordedAtTime; }
}