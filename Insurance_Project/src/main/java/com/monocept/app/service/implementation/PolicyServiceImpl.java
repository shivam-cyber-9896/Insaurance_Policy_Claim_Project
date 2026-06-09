package com.monocept.app.service.implementation;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.monocept.app.dto.PolicyIssueRequestDto;
import com.monocept.app.model.Policy;
import com.monocept.app.model.PolicyPlan;
import com.monocept.app.enums.PolicyStatus;
import com.monocept.app.service.PolicyService;
import com.monocept.app.dto.PolicyPurchaseRequestDto;
import com.monocept.app.dto.PolicyResponseDto;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.Customer;
import com.monocept.app.model.User;
import com.monocept.app.repository.CustomerRepository;
import com.monocept.app.repository.PolicyPlanRepository;
import com.monocept.app.repository.PolicyRepository;
import com.monocept.app.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyServiceImpl implements PolicyService {

	private final PolicyRepository policyRepository;
	private final PolicyPlanRepository planRepository;
	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;

	@Override
	public PolicyResponseDto purchasePolicy(PolicyPurchaseRequestDto dto) {

		log.info("Purchasing policy");

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Customer customer = customerRepository.findByUser(user)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

		PolicyPlan plan = planRepository.findById(dto.getPlanId())
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

		Policy policy = new Policy();

		policy.setCustomer(customer);
		policy.setPolicyPlan(plan);

		policy.setPolicyNumber(UUID.randomUUID().toString().substring(0, 10));

		policy.setStartDate(dto.getStartDate());

		policy.setEndDate(dto.getStartDate().plusYears(plan.getDurationYears()));

		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);

		policy.setTotalPremiumPaid(BigDecimal.ZERO);

		Policy savedPolicy = policyRepository.save(policy);

		log.info("Policy created successfully");

		return convertToDto(savedPolicy);
	}

	@Override
	public PolicyResponseDto issuePolicy(PolicyIssueRequestDto dto) {
		log.info("Issuing policy to customer: {}", dto.getCustomerId());

		Customer customer = customerRepository.findById(dto.getCustomerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

		PolicyPlan plan = planRepository.findById(dto.getPlanId())
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

		Policy policy = new Policy();
		policy.setCustomer(customer);
		policy.setPolicyPlan(plan);
		policy.setPolicyNumber(UUID.randomUUID().toString().substring(0, 10));
		policy.setStartDate(dto.getStartDate());
		policy.setEndDate(dto.getStartDate().plusYears(plan.getDurationYears()));
		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);
		policy.setTotalPremiumPaid(BigDecimal.ZERO);

		Policy savedPolicy = policyRepository.save(policy);

		log.info("Policy issued successfully");

		return convertToDto(savedPolicy);
	}

	@Override
	public PolicyResponseDto getPolicyById(Long id) {

		Policy policy = findPolicyById(id);

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) {
			if (!policy.getCustomer().getUser().getEmail().equals(email)) {
				throw new com.monocept.app.exception.InvalidOperationException("You are not authorized to view this policy");
			}
		}

		return convertToDto(policy);
	}

	@Override
	public PolicyResponseDto cancelPolicy(Long id) {

		Policy policy = findPolicyById(id);

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

		dto.setCustomerName(policy.getCustomer().getUser().getFullName());

		dto.setPlanName(policy.getPolicyPlan().getPlanName());

		return dto;
	}

	@Override
	public Page<PolicyResponseDto> getAllPolicies(Pageable pageable) {
		// TODO Auto-generated method stub
		return policyRepository.findAll(pageable).map(this::convertToDto);
	}

	
}