package com.monocept.app.service.implementation;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.PlanRequestDto;
import com.monocept.app.dto.PlanResponseDto;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.InsuranceProduct;
import com.monocept.app.model.PolicyPlan;
import com.monocept.app.repository.InsuranceProductRepository;
import com.monocept.app.repository.PolicyPlanRepository;
import com.monocept.app.service.PolicyPlanService;



@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyPlanServiceImpl implements PolicyPlanService {

	private final PolicyPlanRepository planRepository;
	private final InsuranceProductRepository productRepository;
	private final ModelMapper modelMapper;

	@Override
	public PlanResponseDto createPlan(PlanRequestDto dto) {

		log.info("Creating policy plan");

		InsuranceProduct product = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		PolicyPlan plan = modelMapper.map(dto, PolicyPlan.class);

		plan.setInsuranceProduct(product);

		PolicyPlan savedPlan = planRepository.save(plan);

		log.info("Policy plan created successfully");

		return convertToDto(savedPlan);
	}

	@Override
	public PlanResponseDto updatePlan(Long id, PlanRequestDto dto) {

		log.info("Updating policy plan: {}", id);

		PolicyPlan plan = findPlanById(id);

		InsuranceProduct product = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		plan.setPlanName(dto.getPlanName());
		plan.setCoverageAmount(dto.getCoverageAmount());
		plan.setPremiumAmount(dto.getPremiumAmount());
		plan.setPremiumType(dto.getPremiumType());
		plan.setDuration(dto.getDuration());
		plan.setTermsAndConditions(dto.getTermsAndConditions());
		plan.setActive(dto.isActive());

		plan.setInsuranceProduct(product);

		PolicyPlan updatedPlan = planRepository.save(plan);

		return convertToDto(updatedPlan);
	}

	@Override
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

	private PolicyPlan findPlanById(Long id) {

		return planRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
	}

	private PlanResponseDto convertToDto(PolicyPlan plan) {

		PlanResponseDto dto = modelMapper.map(plan, PlanResponseDto.class);

		dto.setProductId(plan.getInsuranceProduct().getId());

		dto.setProductName(plan.getInsuranceProduct().getProductName());

		return dto;
	}
}