package com.myopty.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;


    public Payment savePayment(Payment payment) {
        payment.process();
        return paymentRepository.save(payment);
    }


    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}