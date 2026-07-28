package com.monocept.app.service.implementation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.json.JSONObject;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Value;

import com.monocept.app.dto.PaymentRequestDto;
import com.monocept.app.dto.PaymentResponseDto;
import com.monocept.app.dto.RazorpayOrderResponseDto;
import com.monocept.app.dto.RazorpayVerificationRequestDto;
import com.monocept.app.enums.PaymentMode;
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
import com.monocept.app.service.EmailService;
import com.monocept.app.service.EmailTempleteService;
import com.monocept.app.service.PaymentService;

import jakarta.transaction.Transactional;
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
	private final EmailService emailService;
	private final EmailTempleteService emailTemplateService;
	private final RazorpayClient razorpayClient;

	@Value("${razorpay.key.id}")
	private String razorpayKeyId;

	@Value("${razorpay.key.secret}")
	private String razorpayKeySecret;

	@Override
	@Transactional
	public RazorpayOrderResponseDto createRazorpayOrder(Long policyId) {
		log.info("Creating Razorpay order for policyId: {}", policyId);

		Policy policy = policyRepository.findById(policyId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == Role.CUSTOMER) {
			if (!policy.getCustomer().getUser().getEmail().equals(email)) {
				throw new InvalidOperationException("You are not authorized to initiate payment for this policy");
			}
		}

		if (policy.getPolicyStatus() == PolicyStatus.ACTIVE) {
			throw new InvalidOperationException("Premium already paid for this policy");
		}

		BigDecimal expectedPremium = policy.getPolicyPlan().getPremiumAmount();
		long amountInPaise = expectedPremium.multiply(new BigDecimal("100")).longValue();

		try {
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", amountInPaise);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", "rcpt_policy_" + policyId + "_" + System.currentTimeMillis() % 1000000);

			Order order = razorpayClient.orders.create(orderRequest);
			String razorpayOrderId = order.get("id");

			return RazorpayOrderResponseDto.builder()
					.razorpayOrderId(razorpayOrderId)
					.amountInPaise(amountInPaise)
					.amountInRupees(expectedPremium)
					.currency("INR")
					.keyId(razorpayKeyId != null ? razorpayKeyId.trim() : "")
					.policyId(policyId)
					.policyNumber(policy.getPolicyNumber())
					.build();
		} catch (RazorpayException e) {
			log.error("Failed to create Razorpay Order", e);
			throw new InvalidOperationException("Razorpay Order creation failed: " + e.getMessage());
		}
	}

	@Override
	@Transactional
	public PaymentResponseDto verifyAndRecordRazorpayPayment(RazorpayVerificationRequestDto dto) {
		log.info("Verifying Razorpay payment for orderId: {}", dto.getRazorpayOrderId());

		Policy policy = policyRepository.findById(dto.getPolicyId())
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == Role.CUSTOMER) {
			if (!policy.getCustomer().getUser().getEmail().equals(email)) {
				throw new CustomExceptions.UnauthorizedAccessException("You are not authorized to record payment for this policy");
			}
		}

		if (policy.getPolicyStatus() == PolicyStatus.ACTIVE) {
			throw new InvalidOperationException("Premium already paid for this policy");
		}

		try {
			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", dto.getRazorpayOrderId());
			attributes.put("razorpay_payment_id", dto.getRazorpayPaymentId());
			attributes.put("razorpay_signature", dto.getRazorpaySignature());

			String cleanSecret = razorpayKeySecret != null ? razorpayKeySecret.trim() : "";
			boolean isSignatureValid = Utils.verifyPaymentSignature(attributes, cleanSecret);

			if (!isSignatureValid) {
				throw new CustomExceptions.PaymentProcessingException("Invalid Razorpay payment signature verification failed");
			}
		} catch (RazorpayException e) {
			log.error("Razorpay signature verification error", e);
			throw new CustomExceptions.PaymentProcessingException("Razorpay signature verification failed: " + e.getMessage());
		}

		if (paymentRepository.existsByTransactionReference(dto.getRazorpayPaymentId())) {
			throw new DuplicateResourceException("Payment with this Razorpay payment ID already recorded");
		}

		BigDecimal expectedPremium = policy.getPolicyPlan().getPremiumAmount();

		PremiumPayment payment = new PremiumPayment();
		payment.setPolicy(policy);
		payment.setAmount(expectedPremium);
		payment.setPaymentMode(dto.getPaymentMode() != null ? dto.getPaymentMode() : PaymentMode.RAZORPAY);
		payment.setPaymentStatus(PaymentStatus.SUCCESS);
		payment.setTransactionReference(dto.getRazorpayPaymentId());

		PremiumPayment savedPayment = paymentRepository.save(payment);

		policy.setTotalPremiumPaid(policy.getTotalPremiumPaid().add(savedPayment.getAmount()));
		if (policy.getTotalPremiumPaid().compareTo(expectedPremium) >= 0) {
			policy.setPolicyStatus(PolicyStatus.ACTIVE);
		}
		policyRepository.saveAndFlush(policy);

		try {
			emailService.sendEmail(
				policy.getCustomer().getUser().getEmail(),
				"Payment Successful - " + policy.getPolicyNumber(),
				emailTemplateService.paymentSuccessTemplate(
					policy.getCustomer().getUser().getFullName(),
					policy.getPolicyNumber(),
					savedPayment.getAmount().toString(),
					savedPayment.getTransactionReference(),
					savedPayment.getPaymentDate() != null ? savedPayment.getPaymentDate().toString() : java.time.LocalDateTime.now().toString()
				)
			);
		} catch (Exception ex) {
			log.error("Failed to send payment confirmation email", ex);
		}

		return convertToDto(savedPayment);
	}

	@Override
	@Transactional
	public PaymentResponseDto recordPayment(PaymentRequestDto dto) {

		log.info("Recording payment");

		/*
		 * // Check duplicate transaction reference if
		 * (paymentRepository.existsByTransactionReference(dto.getTransactionReference()
		 * )) { throw new
		 * DuplicateResourceException("Transaction reference already exists"); }
		 */

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
		payment.setTransactionReference("TXN-"
			    + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
			    + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());		// Save Payment
		PremiumPayment savedPayment = paymentRepository.save(payment);

		// Update Policy
		if (savedPayment.getPaymentStatus() == PaymentStatus.SUCCESS) {

		    policy.setTotalPremiumPaid(policy.getTotalPremiumPaid().add(savedPayment.getAmount()));

		    if (policy.getTotalPremiumPaid().compareTo(expectedPremium) >= 0) {
		        policy.setPolicyStatus(PolicyStatus.ACTIVE);
		    }

		    policyRepository.saveAndFlush(policy);

		    // send payment success email
		    emailService.sendEmail(
		        policy.getCustomer().getUser().getEmail(),
		        "Payment Successful - " + policy.getPolicyNumber(),
		        emailTemplateService.paymentSuccessTemplate(
		            policy.getCustomer().getUser().getFullName(),
		            policy.getPolicyNumber(),
		            savedPayment.getAmount().toString(),
		            savedPayment.getTransactionReference(),
		            savedPayment.getPaymentDate() != null ? savedPayment.getPaymentDate().toString() : java.time.LocalDateTime.now().toString()
		        )
		    );}

		    if (savedPayment.getPaymentStatus() == PaymentStatus.FAILED) {
		        emailService.sendEmail(
		            policy.getCustomer().getUser().getEmail(),
		            "Payment Failed - " + policy.getPolicyNumber(),
		            emailTemplateService.paymentFailedTemplate(
		                policy.getCustomer().getUser().getFullName(),
		                policy.getPolicyNumber(),
		                savedPayment.getAmount().toString()
		            )
		        );
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