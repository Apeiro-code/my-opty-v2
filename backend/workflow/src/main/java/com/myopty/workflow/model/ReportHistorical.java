package com.myopty.workflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("report_historical")
public class ReportHistorical {

    @Id
    private Integer historicalId;

    private Integer clientId;

    private Integer month;

    private Integer year;

    private String metricType;

    private Double metricValue;

    private java.time.LocalDate recordedAt;

    public ReportHistorical() {
        this.metricType = "";
        this.metricValue = 0.0;
        this.recordedAt = java.time.LocalDate.now();
    }

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

    public java.time.LocalDate getRecordedAt() { return recordedAt; }
    public void setRecordedAt(java.time.LocalDate recordedAt) { this.recordedAt = recordedAt; }
}