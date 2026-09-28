package com.myopty.billing;

import jakarta.persistence.*;

@Entity
@Table(name = "billing_report")
public class BillingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private int reportId;

    @Column(name = "month")
    private String month;

    @Column(name = "total_revenue")
    private double totalRevenue;

    public int getReportId() {
        return reportId;
    }



    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    // Getters සහ Setters මෙතනටත් දාගන්න
}