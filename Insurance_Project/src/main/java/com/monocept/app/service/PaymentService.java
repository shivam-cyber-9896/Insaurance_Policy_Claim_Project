package com.monocept.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.monocept.app.dto.PaymentRequestDto;
import com.monocept.app.dto.PaymentResponseDto;
import com.monocept.app.dto.RazorpayOrderResponseDto;
import com.monocept.app.dto.RazorpayVerificationRequestDto;

public interface PaymentService {

	RazorpayOrderResponseDto createRazorpayOrder(Long policyId);

	PaymentResponseDto verifyAndRecordRazorpayPayment(RazorpayVerificationRequestDto dto);

	PaymentResponseDto recordPayment(PaymentRequestDto dto);

	PaymentResponseDto getPaymentById(Long id);

	List<PaymentResponseDto> getPaymentsByPolicy(Long policyId);

	Page<PaymentResponseDto> getAllPayments(Pageable pageable);

	Page<PaymentResponseDto> getMyPayments(Pageable pageable);
}