package com.monocept.app.service.implementation;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.monocept.app.dto.PolicyIssueRequestDto;
import com.monocept.app.model.Policy;
import com.monocept.app.model.PolicyPlan;
import com.monocept.app.enums.AgentSpecialization;
import com.monocept.app.enums.ClaimStatus;
import com.monocept.app.enums.PolicyStatus;
import com.monocept.app.enums.Role;
import com.monocept.app.exception.InvalidOperationException;
import com.monocept.app.service.EmailService;
import com.monocept.app.service.EmailTempleteService;
import com.monocept.app.service.PolicyService;
import com.monocept.app.service.PremiumCalculatorService;
import com.monocept.app.dto.PolicyPurchaseRequestDto;
import com.monocept.app.dto.PolicyResponseDto;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.Customer;
import com.monocept.app.model.User;
import com.monocept.app.repository.ClaimRepository;
import com.monocept.app.repository.CustomerRepository;
import com.monocept.app.repository.PolicyPlanRepository;
import com.monocept.app.repository.PolicyRepository;
import com.monocept.app.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PolicyServiceImpl implements PolicyService {

	private final PolicyRepository policyRepository;
	private final PolicyPlanRepository planRepository;
	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;
	private final ClaimRepository claimRepository;
	private final ModelMapper modelMapper;

	private final EmailService emailService;
	private final EmailTempleteService emailTemplateService;
	private final PremiumCalculatorService premiumCalculatorService;

	@Override
	@Transactional
	public PolicyResponseDto purchasePolicy(PolicyPurchaseRequestDto dto) {

		log.info("Purchasing policy with runtime calculated premium using customer details");

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Customer customer = customerRepository.findByUser(user)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

		PolicyPlan plan = planRepository.findById(dto.getPlanId())
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

		// ─── Validate selected coverage is within plan's [min, max] range ───
		BigDecimal selectedCoverage = dto.getSelectedCoverageAmount();
		if (selectedCoverage.compareTo(plan.getMinCoverageAmount()) < 0
				|| selectedCoverage.compareTo(plan.getMaxCoverageAmount()) > 0) {
			throw new InvalidOperationException(
					"Selected coverage ₹" + selectedCoverage.toPlainString()
					+ " is outside plan range [₹" + plan.getMinCoverageAmount().toPlainString()
					+ " - ₹" + plan.getMaxCoverageAmount().toPlainString() + "]");
		}

		com.monocept.app.enums.ProductType productType = plan.getInsuranceProduct().getProductType();
		java.util.List<PolicyStatus> activeStatuses = java.util.List.of(PolicyStatus.ACTIVE, PolicyStatus.PENDING_PAYMENT);

		// ─── Validate Aadhaar number (Required for LIFE, HEALTH, TRAVEL; optional for MOTOR) ───
		if (productType != com.monocept.app.enums.ProductType.MOTOR) {
			if (dto.getHolderAadhaar() == null || !dto.getHolderAadhaar().matches("^\\d{12}$")) {
				throw new InvalidOperationException("Aadhaar number is required and must be a 12-digit numeric number.");
			}
			if (policyRepository.existsByHolderAadhaarAndPolicyStatusIn(dto.getHolderAadhaar(), activeStatuses)) {
				throw new InvalidOperationException("Aadhaar number already linked to an active policy.");
			}
		} else if (dto.getHolderAadhaar() != null && !dto.getHolderAadhaar().isBlank()) {
			if (policyRepository.existsByHolderAadhaarAndPolicyStatusIn(dto.getHolderAadhaar(), activeStatuses)) {
				throw new InvalidOperationException("Aadhaar number already linked to an active policy.");
			}
		}

		// ─── Validate vehicle number for MOTOR insurance ───
		String normalizedVehicle = null;
		if (productType == com.monocept.app.enums.ProductType.MOTOR) {
			if (dto.getVehicleNumber() == null || dto.getVehicleNumber().isBlank()) {
				throw new InvalidOperationException("Vehicle number is required for Motor Insurance.");
			}
			normalizedVehicle = dto.getVehicleNumber().toUpperCase().replaceAll("\\s+", "");
			if (!normalizedVehicle.matches("^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$")) {
				throw new InvalidOperationException("Invalid car registration number format (e.g. MH01AB1234).");
			}
			if (policyRepository.existsByVehicleNumberAndPolicyStatusIn(normalizedVehicle, activeStatuses)) {
				throw new InvalidOperationException("Vehicle number already insured under an active policy.");
			}
		} else if (dto.getVehicleNumber() != null && !dto.getVehicleNumber().isBlank()) {
			normalizedVehicle = dto.getVehicleNumber().toUpperCase().replaceAll("\\s+", "");
		}

		// Determine customer age from date of birth
		int customerAge = 30;
		if (customer.getDateOfBirth() != null) {
			customerAge = java.time.Period.between(customer.getDateOfBirth(), java.time.LocalDate.now()).getYears();
			if (customerAge < 18) customerAge = 18;
		}

		// Determine smoker status: from DTO if provided, otherwise customer profile
		boolean isSmoker = Boolean.TRUE.equals(dto.getIsSmoker()) || Boolean.TRUE.equals(customer.getIsSmoker());

		// Determine billing frequency: from DTO if provided, otherwise plan's default frequency
		com.monocept.app.enums.PremiumType selectedFrequency = (plan.getPremiumType() == com.monocept.app.enums.PremiumType.ONE_TIME)
				? com.monocept.app.enums.PremiumType.ONE_TIME
				: (dto.getPremiumType() != null ? dto.getPremiumType() : (plan.getPremiumType() != null ? plan.getPremiumType() : com.monocept.app.enums.PremiumType.ANNUAL));

		// ─── Calculate dynamic premium using Plan Base Premium if set, else Actuarial Calculator ───
		BigDecimal calculatedRuntimePremium;
		if (plan.getPremiumAmount() != null && plan.getPremiumAmount().compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal minCov = (plan.getMinCoverageAmount() != null && plan.getMinCoverageAmount().compareTo(BigDecimal.ZERO) > 0)
					? plan.getMinCoverageAmount()
					: new BigDecimal("50000");

			BigDecimal scaledBase = plan.getPremiumAmount().multiply(selectedCoverage).divide(minCov, 4, java.math.RoundingMode.HALF_UP);

			// Age loading percentage
			BigDecimal ageLoadingPct = BigDecimal.ZERO;
			if (customerAge >= 60) ageLoadingPct = new BigDecimal("0.60");
			else if (customerAge >= 45) ageLoadingPct = new BigDecimal("0.35");
			else if (customerAge >= 30) ageLoadingPct = new BigDecimal("0.15");

			// Smoker loading percentage (Life & Health only)
			BigDecimal smokerLoadingPct = BigDecimal.ZERO;
			if (isSmoker && (productType == com.monocept.app.enums.ProductType.LIFE || productType == com.monocept.app.enums.ProductType.HEALTH)) {
				if (customerAge >= 60) smokerLoadingPct = new BigDecimal("0.75");
				else if (customerAge >= 45) smokerLoadingPct = new BigDecimal("0.50");
				else if (customerAge >= 30) smokerLoadingPct = new BigDecimal("0.25");
				else smokerLoadingPct = new BigDecimal("0.15");
			}

			BigDecimal totalLoadingMultiplier = BigDecimal.ONE.add(ageLoadingPct).add(smokerLoadingPct);
			BigDecimal annualNet = scaledBase.multiply(totalLoadingMultiplier).setScale(2, java.math.RoundingMode.HALF_UP);

			// Billing frequency installment factor
			if (selectedFrequency == com.monocept.app.enums.PremiumType.HALF_YEARLY) {
				calculatedRuntimePremium = annualNet.multiply(new BigDecimal("0.55")).setScale(2, java.math.RoundingMode.HALF_UP);
			} else if (selectedFrequency == com.monocept.app.enums.PremiumType.QUARTERLY) {
				calculatedRuntimePremium = annualNet.multiply(new BigDecimal("0.275")).setScale(2, java.math.RoundingMode.HALF_UP);
			} else {
				calculatedRuntimePremium = annualNet;
			}
		} else {
			com.monocept.app.dto.PremiumCalculatorRequestDto calcReq = com.monocept.app.dto.PremiumCalculatorRequestDto.builder()
					.coverageAmount(selectedCoverage)
					.durationYears(plan.getDurationYears())
					.premiumType(selectedFrequency)
					.productType(productType)
					.age(customerAge)
					.isSmoker(isSmoker)
					.build();

			calculatedRuntimePremium = premiumCalculatorService.calculatePremium(calcReq).getCalculatedPremium();
		}

		log.info("Calculated runtime installment premium: ₹{} for coverage ₹{}, age {}, smoker: {}, frequency: {}",
				calculatedRuntimePremium, selectedCoverage, customerAge, isSmoker, selectedFrequency);

		// Validate and assign agent
		User agent = resolveAgent(dto.getAgentId(), productType);

		Policy policy = new Policy();
		policy.setCustomer(customer);
		policy.setPolicyPlan(plan);
		policy.setAgent(agent);

		policy.setPolicyNumber(
				"POL-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
						+ "-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase());

		policy.setStartDate(dto.getStartDate());
		policy.setEndDate(dto.getStartDate().plusYears(plan.getDurationYears()));
		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);
		policy.setTotalPremiumPaid(BigDecimal.ZERO);

		// ─── Persist new fields ───
		policy.setSelectedCoverageAmount(selectedCoverage);
		policy.setRemainingCoverage(selectedCoverage);
		policy.setPremiumType(selectedFrequency);
		policy.setPremiumAmount(calculatedRuntimePremium);

		// Policyholder details
		policy.setHolderName(dto.getHolderName());
		policy.setHolderAddress(dto.getHolderAddress());
		policy.setHolderPhone(dto.getHolderPhone());
		policy.setHolderAadhaar(dto.getHolderAadhaar());
		policy.setVehicleNumber(normalizedVehicle);

		Policy savedPolicy = policyRepository.save(policy);

		emailService.sendEmail(customer.getUser().getEmail(), "Policy Created - " + savedPolicy.getPolicyNumber(),
				emailTemplateService.policyCreatedTemplate(customer.getUser().getFullName(),
						savedPolicy.getPolicyNumber(), plan.getPlanName(), savedPolicy.getStartDate().toString(),
						savedPolicy.getEndDate().toString(), calculatedRuntimePremium.toString()));

		if (savedPolicy.getAgent() != null) {
			try {
				emailService.sendEmail(savedPolicy.getAgent().getEmail(), "New Policy Assigned - " + savedPolicy.getPolicyNumber(),
						emailTemplateService.agentAssignedTemplate(savedPolicy.getAgent().getFullName(),
								customer.getUser().getFullName(), savedPolicy.getPolicyNumber()));
			} catch (Exception e) {
				log.error("Failed to send email to agent: ", e);
			}
		}

		log.info("Policy created successfully with coverage ₹{}, frequency {}", selectedCoverage, selectedFrequency);

		return convertToDto(savedPolicy);
	}

	@Override
	@Transactional
	public PolicyResponseDto issuePolicy(PolicyIssueRequestDto dto) {
		log.info("Issuing policy to customer: {}", dto.getCustomerId());

		Customer customer = customerRepository.findById(dto.getCustomerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

		PolicyPlan plan = planRepository.findById(dto.getPlanId())
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

		// Validate and assign agent
		User agent = resolveAgent(dto.getAgentId(), plan.getInsuranceProduct().getProductType());

		Policy policy = new Policy();
		policy.setCustomer(customer);
		policy.setPolicyPlan(plan);
		policy.setAgent(agent);
		policy.setPolicyNumber(
				"POL-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
						+ "-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase());
		policy.setStartDate(dto.getStartDate());
		policy.setEndDate(dto.getStartDate().plusYears(plan.getDurationYears()));
		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);
		policy.setTotalPremiumPaid(BigDecimal.ZERO);
		policy.setRemainingCoverage(plan.getMinCoverageAmount());

		Policy savedPolicy = policyRepository.save(policy);

		emailService.sendEmail(customer.getUser().getEmail(), "Policy Issued - " + savedPolicy.getPolicyNumber(),
				emailTemplateService.policyCreatedTemplate(customer.getUser().getFullName(),
						savedPolicy.getPolicyNumber(), plan.getPlanName(), savedPolicy.getStartDate().toString(),
						savedPolicy.getEndDate().toString(), plan.getPremiumAmount().toString()));

		if (savedPolicy.getAgent() != null) {
			try {
				emailService.sendEmail(savedPolicy.getAgent().getEmail(), "New Policy Assigned - " + savedPolicy.getPolicyNumber(),
						emailTemplateService.agentAssignedTemplate(savedPolicy.getAgent().getFullName(),
								customer.getUser().getFullName(), savedPolicy.getPolicyNumber()));
			} catch (Exception e) {
				log.error("Failed to send email to agent: ", e);
			}
		}

		log.info("Policy issued successfully");

		return convertToDto(savedPolicy);
	}

	/**
	 * Validates agent eligibility for the given product type.
	 * - SUPER_AGENT (specialization=SUPER) can be assigned to any product type.
	 * - Standard AGENT must have specialization matching the product type.
	 */
	private User resolveAgent(Long agentId, com.monocept.app.enums.ProductType productType) {
		if (agentId == null) {
			return null;
		}

		User agent = userRepository.findById(agentId)
				.orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));

		if (agent.getRole() != Role.AGENT && agent.getRole() != Role.SUPER_AGENT) {
			throw new InvalidOperationException("Selected user is not an agent");
		}

		if (!agent.isActive()) {
			throw new InvalidOperationException("Selected agent is inactive");
		}

		// SUPER_AGENT can handle all types
		if (agent.getSpecialization() == AgentSpecialization.SUPER) {
			return agent;
		}

		// Standard AGENT — specialization must match the policy product type
		if (agent.getSpecialization() == null ||
				!agent.getSpecialization().name().equals(productType.name())) {
			throw new InvalidOperationException(
					"Agent specialization [" + agent.getSpecialization() + "] does not match policy type [" + productType + "]. " +
					"Please select an agent with " + productType + " specialization or a SUPER_AGENT.");
		}

		return agent;
	}

	@Override
	public PolicyResponseDto getPolicyById(Long id) {

		Policy policy = findPolicyById(id);

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) {
			if (!policy.getCustomer().getUser().getEmail().equals(email)) {
				throw new com.monocept.app.exception.InvalidOperationException(
						"You are not authorized to view this policy");
			}
		}

		return convertToDto(policy);
	}

	@Override
	@Transactional
	public PolicyResponseDto cancelPolicy(Long id) {

		Policy policy = findPolicyById(id);

		// Block cancellation if any claim is still pending (not yet APPROVED or REJECTED)
		boolean hasPendingClaims = claimRepository.existsByPolicyIdAndClaimStatusIn(
				policy.getId(),
				java.util.List.of(ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW, ClaimStatus.RECOMMENDED));

		if (hasPendingClaims) {
			throw new InvalidOperationException(
					"Cannot cancel policy '" + policy.getPolicyNumber() + "' because it has pending claims. "
					+ "All claims must be resolved (APPROVED or REJECTED) before cancellation.");
		}

		policy.setPolicyStatus(PolicyStatus.CANCELLED);

		return convertToDto(policyRepository.save(policy));
	}

	@Override
	public Page<PolicyResponseDto> getMyPolicies(Pageable pageable) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Customer customer = customerRepository.findByUser(user)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
		return policyRepository.findByCustomer(customer, pageable).map(this::convertToDto);
	}

	private Policy findPolicyById(Long id) {

		return policyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
	}

	private PolicyResponseDto convertToDto(Policy policy) {

		PolicyResponseDto dto = modelMapper.map(policy, PolicyResponseDto.class);

		dto.setId(policy.getId());
		dto.setCustomerId(policy.getCustomer() != null ? policy.getCustomer().getId() : null);
		dto.setCustomerName(policy.getCustomer() != null && policy.getCustomer().getUser() != null ? policy.getCustomer().getUser().getFullName() : null);

		dto.setPlanId(policy.getPolicyPlan() != null ? policy.getPolicyPlan().getId() : null);
		dto.setPlanName(policy.getPolicyPlan() != null ? policy.getPolicyPlan().getPlanName() : null);

		if (policy.getPolicyPlan() != null && policy.getPolicyPlan().getInsuranceProduct() != null) {
			dto.setProductType(policy.getPolicyPlan().getInsuranceProduct().getProductType());
		}
		
		// If policy has a selected coverage amount (purchased via dynamic range), use it; otherwise fallback to plan min
		dto.setSelectedCoverageAmount(policy.getSelectedCoverageAmount() != null 
				? policy.getSelectedCoverageAmount() 
				: policy.getPolicyPlan().getMinCoverageAmount());
		dto.setCoverageAmount(dto.getSelectedCoverageAmount());

		// Use locked premium amount & type from policy if present; fallback to plan defaults
		dto.setPremiumAmount(policy.getPremiumAmount() != null 
				? policy.getPremiumAmount() 
				: policy.getPolicyPlan().getPremiumAmount());
		dto.setPremiumType(policy.getPremiumType() != null 
				? policy.getPremiumType() 
				: policy.getPolicyPlan().getPremiumType());

		dto.setRemainingCoverage(policy.getRemainingCoverage());

		// Policyholder details
		dto.setHolderName(policy.getHolderName());
		dto.setHolderAddress(policy.getHolderAddress());
		dto.setHolderPhone(policy.getHolderPhone());
		dto.setVehicleNumber(policy.getVehicleNumber());

		// Mask Aadhaar: "XXXX-XXXX-1234" for security
		if (policy.getHolderAadhaar() != null && policy.getHolderAadhaar().length() == 12) {
			dto.setHolderAadhaar("XXXX-XXXX-" + policy.getHolderAadhaar().substring(8));
		} else {
			dto.setHolderAadhaar(policy.getHolderAadhaar());
		}

		if (policy.getAgent() != null) {
			dto.setAgentId(policy.getAgent().getId());
			dto.setAgentName(policy.getAgent().getFullName());
		}

		return dto;
	}

	@Override
	public Page<PolicyResponseDto> getAllPolicies(Pageable pageable) {
		// TODO Auto-generated method stub
		return policyRepository.findAll(pageable).map(this::convertToDto);
	}

}