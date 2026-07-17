package com.monocept.app.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.monocept.app.enums.ClaimStatus;
import com.monocept.app.enums.ProductType;
import com.monocept.app.model.Claim;
import com.monocept.app.model.Policy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
	
    List<Claim> findByPolicy(Policy policy);

    List<Claim> findByClaimStatus(ClaimStatus claimStatus);
    
    boolean existsByClaimNumber(String claimNumber);

    boolean existsByPolicyIdAndClaimAmountAndIncidentDate(Long policyId, java.math.BigDecimal claimAmount, java.time.LocalDate incidentDate);

    /**
     * Returns true if the given policy has at least one claim with any of the given statuses.
     * Used to block policy cancellation when unresolved claims exist.
     */
    boolean existsByPolicyIdAndClaimStatusIn(Long policyId, java.util.List<ClaimStatus> statuses);

    Page<Claim> findByPolicyCustomer(com.monocept.app.model.Customer customer, Pageable pageable);

    /**
     * Super rule: find all RECOMMENDED claims of a specific product type
     * whose amount is within the agent's/threshold limit.
     */
    @Query("SELECT c FROM Claim c " +
           "WHERE c.claimStatus = :status " +
           "AND c.policy.policyPlan.insuranceProduct.productType = :productType " +
           "AND c.claimAmount <= :amountThreshold")
    List<Claim> findRecommendedClaimsForSuperRule(
            @Param("status") ClaimStatus status,
            @Param("productType") ProductType productType,
            @Param("amountThreshold") BigDecimal amountThreshold);
}

