package com.example.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.enums.PolicyStatus;
import com.example.app.model.Customer;
import com.example.app.model.Policy;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

	boolean existsByPolicyNumber(
            String policyNumber);
	
	List<Policy> findByCustomer(
            Customer customer);

    List<Policy> findByPolicyStatus(
            PolicyStatus policyStatus);
}
