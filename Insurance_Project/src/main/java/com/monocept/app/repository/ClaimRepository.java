package com.monocept.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.enums.ClaimStatus;
import com.monocept.app.model.Claim;
import com.monocept.app.model.Policy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
	
	List<Claim> findByPolicy(
            Policy policy);

    List<Claim> findByClaimStatus(
            ClaimStatus claimStatus);
    
    boolean existsByClaimNumber(
            String claimNumber);

    Page<Claim> findByPolicyCustomer(com.monocept.app.model.Customer customer, Pageable pageable);
}
