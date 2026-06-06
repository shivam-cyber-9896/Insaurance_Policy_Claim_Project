package com.monocept.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.model.Policy;
import com.monocept.app.model.PremiumPayment;

public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long> {

	boolean existsByTransactionReference(String transactionReference);

	List<PremiumPayment> findByPolicyId(Long policyId);

	org.springframework.data.domain.Page<PremiumPayment> findByPolicyCustomer(com.monocept.app.model.Customer customer, org.springframework.data.domain.Pageable pageable);
}
