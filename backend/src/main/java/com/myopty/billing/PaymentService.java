package com.myopty.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import com.myopty.billing.Invoice;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    public Payment savePayment(Payment payment) {
        Payment savedPayment = paymentRepository.save(payment);

        Invoice invoice = new Invoice();

        invoice.setPayment(savedPayment);
        invoice.setIssueDate(new Date());
        invoice.setTotalAmount(savedPayment.amount);

        invoiceRepository.save(invoice);

        return savedPayment;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}