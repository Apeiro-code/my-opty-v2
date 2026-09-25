package com.myopty.billing;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int invoiceId;

    public Date issueDate;
    public double totalAmount;

    public void generate() {
        this.issueDate = new Date();
        System.out.println("Invoice ID: " + this.invoiceId + " generated successfully!");
        System.out.println("Total Amount: Rs." + this.totalAmount);
    }
}