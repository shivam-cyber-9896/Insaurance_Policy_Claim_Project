package com.monocept.app.service.implementation;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monocept.app.dto.PlanRequestDto;
import com.monocept.app.dto.PlanResponseDto;
import com.monocept.app.exception.InvalidOperationException;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.InsuranceProduct;
import com.monocept.app.model.PolicyPlan;
import com.monocept.app.repository.InsuranceProductRepository;
import com.monocept.app.repository.PolicyPlanRepository;
import com.monocept.app.service.PolicyPlanService;



@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PolicyPlanServiceImpl implements PolicyPlanService {

	private final PolicyPlanRepository planRepository;
	private final InsuranceProductRepository productRepository;
	private final ModelMapper modelMapper;
	private final com.monocept.app.service.PremiumCalculatorService premiumCalculatorService;

	@Override
	@Transactional
	public PlanResponseDto createPlan(PlanRequestDto dto) {

		log.info("Creating policy plan with admin-specified or actuarially calculated premium");

		InsuranceProduct product = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		java.math.BigDecimal formulaCalculatedPremium = calculatePremiumForPlan(dto, product);

		java.math.BigDecimal effectivePremium = (dto.getPremiumAmount() != null && dto.getPremiumAmount().compareTo(java.math.BigDecimal.ZERO) > 0)
				? dto.getPremiumAmount()
				: formulaCalculatedPremium;

		if (dto.getMinCoverageAmount() != null
				&& dto.getMinCoverageAmount().compareTo(effectivePremium) < 0) {
			throw new InvalidOperationException("Plan coverage amount cannot be less than the plan premium amount");
		}

		PolicyPlan plan = new PolicyPlan();
		plan.setInsuranceProduct(product);
		plan.setPlanName(dto.getPlanName());
		plan.setMinCoverageAmount(dto.getMinCoverageAmount());
		plan.setPremiumAmount(effectivePremium);
		plan.setPremiumType(dto.getPremiumType());
		plan.setDurationYears(dto.getDurationYears());
		plan.setTermsAndConditions(dto.getTermsAndConditions());
		plan.setActive(dto.isActive());

		PolicyPlan savedPlan = planRepository.save(plan);

		log.info("Policy plan created successfully with premium: {}", savedPlan.getPremiumAmount());

		return convertToDto(savedPlan);
	}

	@Override
	@Transactional
	public PlanResponseDto updatePlan(Long id, PlanRequestDto dto) {

		log.info("Updating policy plan: {}", id);

		PolicyPlan plan = findPlanById(id);

		InsuranceProduct product = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		java.math.BigDecimal formulaCalculatedPremium = calculatePremiumForPlan(dto, product);

		java.math.BigDecimal effectivePremium = (dto.getPremiumAmount() != null && dto.getPremiumAmount().compareTo(java.math.BigDecimal.ZERO) > 0)
				? dto.getPremiumAmount()
				: formulaCalculatedPremium;

		if (dto.getMinCoverageAmount() != null
				&& dto.getMinCoverageAmount().compareTo(effectivePremium) < 0) {
			throw new InvalidOperationException("Plan coverage amount cannot be less than the plan premium amount");
		}

		plan.setPlanName(dto.getPlanName());
		plan.setMinCoverageAmount(dto.getMinCoverageAmount());
		plan.setPremiumAmount(effectivePremium);
		plan.setPremiumType(dto.getPremiumType());
		plan.setDurationYears(dto.getDurationYears());
		plan.setTermsAndConditions(dto.getTermsAndConditions());
		plan.setActive(dto.isActive());

		plan.setInsuranceProduct(product);

		PolicyPlan updatedPlan = planRepository.save(plan);

		log.info("Policy plan {} updated successfully with premium: {}", id, effectivePremium);

		return convertToDto(updatedPlan);
	}

	@Override
	@Transactional
	public PlanResponseDto deactivatePlan(Long id) {

		log.info("Deactivating plan: {}", id);

		PolicyPlan plan = findPlanById(id);

		plan.setActive(false);

		return convertToDto(planRepository.save(plan));
	}

	@Override
	public PlanResponseDto getPlanById(Long id) {

		return convertToDto(findPlanById(id));
	}

	
	@Override
	public List<PlanResponseDto> getPlansByProduct(Long productId) {

	    return planRepository
	            .findByInsuranceProductId(productId)
	            .stream()
	            .map(this::convertToDto)
	            .toList();
	}

	@Override
	public Page<PlanResponseDto> getAllPlans(Pageable pageable) {

		return planRepository.findAll(pageable).map(this::convertToDto);
	}

	/**
	 * Runs the actuarial premium formula (Risk Premium + Loading Charges - Discounts)
	 * for the given request against the given product. Used by both create and update
	 * so that the persisted premiumAmount is always the freshly computed, runtime value
	 * and never a value supplied directly by the client.
	 */
	private java.math.BigDecimal calculatePremiumForPlan(PlanRequestDto dto, InsuranceProduct product) {

		com.monocept.app.dto.PremiumCalculatorRequestDto calcReq = com.monocept.app.dto.PremiumCalculatorRequestDto.builder()
				.coverageAmount(dto.getMinCoverageAmount())
				.durationYears(dto.getDurationYears())
				.premiumType(dto.getPremiumType())
				.productType(product.getProductType())
				.age(30)
				.build();

		return premiumCalculatorService.calculatePremium(calcReq).getCalculatedPremium();
	}

	private PolicyPlan findPlanById(Long id) {

		return planRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
	}

	private PlanResponseDto convertToDto(PolicyPlan plan) {

		PlanResponseDto dto = modelMapper.map(plan, PlanResponseDto.class);

		dto.setProductId(plan.getInsuranceProduct().getId());
		dto.setProductName(plan.getInsuranceProduct().getProductName());

		if (plan.getMinCoverageAmount() != null) {
			dto.setMinCoverageAmount(plan.getMinCoverageAmount());
			dto.setMaxCoverageAmount(plan.getMaxCoverageAmount());
		}

		return dto;
	}
}