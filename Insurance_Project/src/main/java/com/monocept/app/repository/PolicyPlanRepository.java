package com.monocept.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.model.InsuranceProduct;
import com.monocept.app.model.PolicyPlan;

public interface PolicyPlanRepository extends JpaRepository<PolicyPlan, Long> {
	
	List<PolicyPlan>
    findByInsuranceProductAndActiveTrue(
        InsuranceProduct product);

List<PolicyPlan> findByActiveTrue();
}
