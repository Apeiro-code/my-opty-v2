package com.myopty.billing;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private int invoiceId;

    // Payment එකත් එක්ක සම්බන්ධ කරන Foreign Key එක
    @Column(name = "payment_id")
    private int paymentId;

    @Temporal(TemporalType.DATE)
    @Column(name = "issue_date")
    private Date issueDate;

    @Column(name = "total_amount")
    private double totalAmount;

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

}