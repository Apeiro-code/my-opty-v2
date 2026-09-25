package com.myopty.billing;

import jakarta.persistence.*;

@Entity
@Table(name = "billing_report")
public class BillingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int reportId;

    public String month;
    public double totalRevenue;

    public void generateReport() {
        System.out.println("Generating billing report for month: " + this.month);
        System.out.println("Total Revenue: Rs." + this.totalRevenue);
    }
}