package com.monocept.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.monocept.app.dto.PaymentRequestDto;
import com.monocept.app.dto.PaymentResponseDto;

public interface PaymentService {

	PaymentResponseDto recordPayment(PaymentRequestDto dto);

	PaymentResponseDto getPaymentById(Long id);

	List<PaymentResponseDto> getPaymentsByPolicy(Long policyId);

	Page<PaymentResponseDto> getAllPayments(Pageable pageable);

	Page<PaymentResponseDto> getMyPayments(Pageable pageable);
}