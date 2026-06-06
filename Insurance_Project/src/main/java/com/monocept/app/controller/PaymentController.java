package com.monocept.app.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.monocept.app.dto.PaymentRequestDto;
import com.monocept.app.dto.PaymentResponseDto;
import com.monocept.app.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> recordPayment(@Valid @RequestBody PaymentRequestDto dto) {

        return ResponseEntity.ok(paymentService.recordPayment(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {

        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponseDto>> getAllPayments(Pageable pageable) {

        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<PaymentResponseDto>> getMyPayments(Pageable pageable) {

        return ResponseEntity.ok(paymentService.getMyPayments(pageable));
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByPolicy(@PathVariable Long policyId) {

        return ResponseEntity.ok(paymentService.getPaymentsByPolicy(policyId));
    }
}