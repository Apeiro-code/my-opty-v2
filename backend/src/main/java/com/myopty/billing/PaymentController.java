package com.myopty.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;


    @PostMapping("/process")
    public Payment processPayment(@RequestBody Payment payment) {
        return paymentService.processPayment(payment);
    }


    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }


    @GetMapping("/filter/status")
    public List<Payment> getPaymentsByStatus(@RequestParam String status) {
        return paymentService.getPaymentsByStatus(status);
    }
}