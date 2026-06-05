package com.monocept.app.service.implementation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.PaymentRequestDto;
import com.monocept.app.dto.PaymentResponseDto;
import com.monocept.app.enums.PaymentStatus;
import com.monocept.app.enums.PolicyStatus;
import com.monocept.app.exception.*;
import com.monocept.app.exception.CustomExceptions.DuplicateResourceException;
import com.monocept.app.model.Policy;
import com.monocept.app.model.PremiumPayment;
import com.monocept.app.repository.PolicyRepository;
import com.monocept.app.repository.PremiumPaymentRepository;
import com.monocept.app.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	private final PremiumPaymentRepository paymentRepository;
	private final PolicyRepository policyRepository;
	private final ModelMapper modelMapper;

	@Override
	public PaymentResponseDto recordPayment(PaymentRequestDto dto) {

		log.info("Recording payment");

		if (paymentRepository.existsByTransactionReference(dto.getTransactionReference())) {

			throw new DuplicateResourceException("Transaction reference already exists");
		}

		Policy policy = policyRepository.findById(dto.getPolicyId())
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

		PremiumPayment payment = modelMapper.map(dto, PremiumPayment.class);

		payment.setPolicy(policy);

		PremiumPayment savedPayment = paymentRepository.save(payment);

		if (savedPayment.getPaymentStatus() == PaymentStatus.SUCCESS) {

			policy.setTotalPremiumPaid(policy.getTotalPremiumPaid().add(savedPayment.getAmount()));

			if (policy.getTotalPremiumPaid()
					.compareTo(BigDecimal.valueOf(policy.getPolicyPlan().getPremiumAmount())) >= 0) {

				policy.setPolicyStatus(PolicyStatus.ACTIVE);
			}

			policyRepository.save(policy);
		}

		return convertToDto(savedPayment);
	}

	@Override
	public PaymentResponseDto getPaymentById(Long id) {

		PremiumPayment payment = findPaymentById(id);

		return convertToDto(payment);
	}

	@Override
	public List<PaymentResponseDto> getPaymentsByPolicy(Long policyId) {

		return paymentRepository.findByPolicyId(policyId).stream().map(this::convertToDto).toList();
	}

	@Override
	public Page<PaymentResponseDto> getAllPayments(Pageable pageable) {

		return paymentRepository.findAll(pageable).map(this::convertToDto);
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