package com.monocept.app.util;

import com.monocept.app.enums.ClaimStatus;
import com.monocept.app.model.Claim;
import com.monocept.app.model.Policy;
import com.monocept.app.repository.PolicyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyCoverageMigration {

    private final PolicyRepository policyRepository;

    @PostConstruct
    @Transactional
    public void migrate() {
        log.info("Starting data migration to backfill remainingCoverage for policies...");
        List<Policy> policies = policyRepository.findAll();
        int migratedCount = 0;
        
        for (Policy policy : policies) {
            if (policy.getRemainingCoverage() == null) {
                BigDecimal totalCoverage = policy.getPolicyPlan().getCoverageAmount();
                BigDecimal nonRejectedClaimsSum = BigDecimal.ZERO;
                
                if (policy.getClaims() != null) {
                    for (Claim claim : policy.getClaims()) {
                        if (claim.getClaimStatus() != ClaimStatus.REJECTED) {
                            nonRejectedClaimsSum = nonRejectedClaimsSum.add(claim.getClaimAmount());
                        }
                    }
                }
                
                BigDecimal remainingCoverage = totalCoverage.subtract(nonRejectedClaimsSum);
                if (remainingCoverage.compareTo(BigDecimal.ZERO) < 0) {
                    remainingCoverage = BigDecimal.ZERO;
                }
                
                policy.setRemainingCoverage(remainingCoverage);
                policyRepository.save(policy);
                migratedCount++;
            }
        }
        
        log.info("Data migration completed. Migrated {} policies.", migratedCount);
    }
}
