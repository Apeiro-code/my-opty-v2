package com.myopty.billing;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByStatus(String status);

    List<Payment> findByPaymentDateBetween(Date startDate, Date endDate);
}
