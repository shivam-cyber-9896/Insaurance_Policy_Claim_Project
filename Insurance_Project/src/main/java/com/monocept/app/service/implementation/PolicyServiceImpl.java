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

		// Determine customer age from date of birth
		int customerAge = 30;
		if (customer.getDateOfBirth() != null) {
			customerAge = java.time.Period.between(customer.getDateOfBirth(), java.time.LocalDate.now()).getYears();
			if (customerAge < 18) customerAge = 18;
		}

		// Calculate dynamic actuarial premium at runtime according to formula based on user's exact age
		com.monocept.app.dto.PremiumCalculatorRequestDto calcReq = com.monocept.app.dto.PremiumCalculatorRequestDto.builder()
				.coverageAmount(plan.getCoverageAmount())
				.durationYears(plan.getDurationYears())
				.premiumType(plan.getPremiumType())
				.productType(plan.getInsuranceProduct().getProductType())
				.age(customerAge)
				.build();

		BigDecimal calculatedRuntimePremium = premiumCalculatorService.calculatePremium(calcReq).getCalculatedPremium();

		// Bind runtime premium: if plan premium was not explicitly set by admin, use calculated formula premium.
		// If admin entered a custom base premium, apply runtime age risk loading for applicants older than 30.
		if (plan.getPremiumAmount() == null || plan.getPremiumAmount().compareTo(BigDecimal.ZERO) <= 0) {
			plan.setPremiumAmount(calculatedRuntimePremium);
			planRepository.save(plan);
		} else if (customerAge > 30) {
			BigDecimal ageLoadingPercent;
			if (customerAge < 45) {
				ageLoadingPercent = new BigDecimal("0.15"); // +15% risk loading for age 30-44
			} else if (customerAge < 60) {
				ageLoadingPercent = new BigDecimal("0.35"); // +35% risk loading for age 45-59
			} else {
				ageLoadingPercent = new BigDecimal("0.60"); // +60% risk loading for age 60+
			}
			BigDecimal ageAdjustedPremium = plan.getPremiumAmount()
					.multiply(BigDecimal.ONE.add(ageLoadingPercent))
					.setScale(2, java.math.RoundingMode.HALF_UP);
			log.info("Applied runtime age loading (+{}%) for customer age {}. Base Premium: ₹{}, Adjusted Premium: ₹{}",
					ageLoadingPercent.multiply(new BigDecimal("100")).toPlainString(), customerAge, plan.getPremiumAmount(), ageAdjustedPremium);
			plan.setPremiumAmount(ageAdjustedPremium);
			planRepository.save(plan);
		}

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
		policy.setRemainingCoverage(plan.getCoverageAmount());

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

		log.info("Policy created successfully");

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
		policy.setRemainingCoverage(plan.getCoverageAmount());

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

		dto.setCustomerName(policy.getCustomer().getUser().getFullName());

		dto.setPlanName(policy.getPolicyPlan().getPlanName());

		dto.setProductType(policy.getPolicyPlan().getInsuranceProduct().getProductType());
		dto.setCoverageAmount(policy.getPolicyPlan().getCoverageAmount());
		dto.setPremiumAmount(policy.getPolicyPlan().getPremiumAmount());
		dto.setPremiumType(policy.getPolicyPlan().getPremiumType());
		dto.setRemainingCoverage(policy.getRemainingCoverage());

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