package com.myopty.billing;

import jakarta.persistence.*;
import java.util.Date;

@Entity

@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int paymentId;
    public int orderId;
    public int methodId;
    public double amount;
    public String status;
    public Date paymentDate;

    public void process() {
        this.paymentDate = new Date();
        this.status = "Completed";
        System.out.println("Payment ID: " + this.paymentId + " processed successfully!");
    }
}