package com.example.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.model.InsuranceProduct;
import com.example.app.model.PolicyPlan;

public interface PolicyPlanRepository extends JpaRepository<PolicyPlan, Long> {
	
	List<PolicyPlan>
    findByInsuranceProductAndActiveTrue(
        InsuranceProduct product);

List<PolicyPlan> findByActiveTrue();
}
