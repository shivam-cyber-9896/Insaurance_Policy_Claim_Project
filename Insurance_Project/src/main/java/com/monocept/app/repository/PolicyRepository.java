package com.monocept.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.enums.PolicyStatus;
import com.monocept.app.model.Customer;
import com.monocept.app.model.Policy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PolicyRepository
        extends JpaRepository<Policy, Long> {

    boolean existsByPolicyNumber(String policyNumber);

    List<Policy> findByCustomer(Customer customer);
    
    Page<Policy> findByCustomer(Customer customer, Pageable pageable);

    List<Policy> findByPolicyStatus(
            PolicyStatus policyStatus);
}