package com.monocept.app.service.implementation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.PaymentRequestDto;
import com.monocept.app.dto.PaymentResponseDto;
import com.monocept.app.enums.PaymentStatus;
import com.monocept.app.enums.PolicyStatus;
import com.monocept.app.enums.Role;
import com.monocept.app.exception.*;
import com.monocept.app.exception.CustomExceptions.DuplicateResourceException;
import com.monocept.app.model.Policy;
import com.monocept.app.model.PremiumPayment;
import com.monocept.app.model.User;
import com.monocept.app.model.Customer;
import com.monocept.app.repository.PolicyRepository;
import com.monocept.app.repository.PremiumPaymentRepository;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.repository.CustomerRepository;
import com.monocept.app.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	private final PremiumPaymentRepository paymentRepository;
	private final PolicyRepository policyRepository;
	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final ModelMapper modelMapper;

	@Override
	public PaymentResponseDto recordPayment(PaymentRequestDto dto) {

		log.info("Recording payment");

		// Check duplicate transaction reference
		if (paymentRepository.existsByTransactionReference(dto.getTransactionReference())) {
			throw new DuplicateResourceException("Transaction reference already exists");
		}

		// Find policy
		Policy policy = policyRepository.findById(dto.getPolicyId())
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

		// Check logged-in user
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Customer can only pay for own policy
		if (loggedInUser.getRole() == Role.CUSTOMER) {

			if (!policy.getCustomer().getUser().getEmail().equals(email)) {

				throw new InvalidOperationException("You are not authorized to make payment for this policy");
			}
		}

		// Prevent payment if already active
		if (policy.getPolicyStatus() == PolicyStatus.ACTIVE) {
			throw new InvalidOperationException("Premium already paid for this policy");
		}

		// Validate exact premium amount
		BigDecimal expectedPremium = policy.getPolicyPlan().getPremiumAmount();

		if (dto.getAmount().compareTo(expectedPremium) != 0) {

			throw new InvalidOperationException("Premium amount must be exactly ₹" + expectedPremium);
		}

		// Map DTO to Entity
		PremiumPayment payment = modelMapper.map(dto, PremiumPayment.class);

		payment.setPolicy(policy);

		// Save Payment
		PremiumPayment savedPayment = paymentRepository.save(payment);

		// Update Policy
		if (savedPayment.getPaymentStatus() == PaymentStatus.SUCCESS) {

			policy.setTotalPremiumPaid(policy.getTotalPremiumPaid().add(savedPayment.getAmount()));

			if (policy.getTotalPremiumPaid().compareTo(expectedPremium) >= 0) {

				policy.setPolicyStatus(PolicyStatus.ACTIVE);
			}

			policyRepository.save(policy);
		}

		return convertToDto(savedPayment);
	}

	@Override
	public PaymentResponseDto getPaymentById(Long id) {

		PremiumPayment payment = findPaymentById(id);

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) {
			if (!payment.getPolicy().getCustomer().getUser().getEmail().equals(email)) {
				throw new com.monocept.app.exception.InvalidOperationException(
						"You are not authorized to view this payment");
			}
		}

		return convertToDto(payment);
	}

	@Override
	public List<PaymentResponseDto> getPaymentsByPolicy(Long policyId) {

		Policy policy = policyRepository.findById(policyId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) {
			if (!policy.getCustomer().getUser().getEmail().equals(email)) {
				throw new com.monocept.app.exception.InvalidOperationException(
						"You are not authorized to view payments for this policy");
			}
		}

		return paymentRepository.findByPolicyId(policyId).stream().map(this::convertToDto).toList();
	}

	@Override
	public Page<PaymentResponseDto> getAllPayments(Pageable pageable) {

		return paymentRepository.findAll(pageable).map(this::convertToDto);
	}

	@Override
	public Page<PaymentResponseDto> getMyPayments(Pageable pageable) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Customer customer = customerRepository.findByUser(user)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
		return paymentRepository.findByPolicyCustomer(customer, pageable).map(this::convertToDto);
	}

	private PremiumPayment findPaymentById(Long id) {

		return paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
	}

	private PaymentResponseDto convertToDto(PremiumPayment payment) {

		PaymentResponseDto dto = modelMapper.map(payment, PaymentResponseDto.class);

		dto.setPolicyNumber(payment.getPolicy().getPolicyNumber());

		return dto;
	}
}