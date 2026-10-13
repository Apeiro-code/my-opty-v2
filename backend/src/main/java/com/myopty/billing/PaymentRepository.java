package com.myopty.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {


    List<Payment> findByStatus(String status);


    List<Payment> findByPaymentDateBetween(Date startDate, Date endDate);
}