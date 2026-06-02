package com.example.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.model.Policy;
import com.example.app.model.PremiumPayment;

public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long> {

	boolean existsByTransactionReference(
            String transactionReference);

    List<PremiumPayment> findByPolicy(
            Policy policy);
}
