package com.example.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.enums.ClaimStatus;
import com.example.app.model.Claim;
import com.example.app.model.Policy;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
	
	List<Claim> findByPolicy(
            Policy policy);

    List<Claim> findByClaimStatus(
            ClaimStatus claimStatus);
    
    boolean existsByClaimNumber(
            String claimNumber);

}
