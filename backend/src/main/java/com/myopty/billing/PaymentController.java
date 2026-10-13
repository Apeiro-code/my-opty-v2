package com.myopty.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(
            @RequestParam("orderId") int orderId,
            @RequestParam("methodId") int methodId,
            @RequestParam("amount") Double amount,
            @RequestParam("status") String status,
            @RequestParam(value = "slipImage", required = false) MultipartFile slipImage) {

        try {
            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setMethodId(methodId);
            payment.setAmount(amount);
            payment.setStatus(status);

            // බැංකු ස්ලිප් එකක් (Image එකක්) ඇවිත් තියෙනවා නම් සේව් කරගැනීම
            if (slipImage != null && !slipImage.isEmpty()) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + slipImage.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.write(filePath, slipImage.getBytes());
            }

            Payment savedPayment = paymentService.processPayment(payment);
            return ResponseEntity.ok(savedPayment);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload payment slip image.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing payment.");
        }
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