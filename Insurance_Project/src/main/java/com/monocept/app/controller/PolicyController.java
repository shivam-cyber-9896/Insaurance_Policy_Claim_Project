package com.monocept.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monocept.app.dto.PolicyIssueRequestDto;
import com.monocept.app.dto.PolicyPurchaseRequestDto;
import com.monocept.app.dto.PolicyResponseDto;
import com.monocept.app.service.PolicyService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

	private final PolicyService policyService;

	@PostMapping("/purchase")
	public ResponseEntity<PolicyResponseDto> purchasePolicy(@Valid @RequestBody PolicyPurchaseRequestDto dto) {

		return ResponseEntity.ok(policyService.purchasePolicy(dto));
	}

	@PostMapping("/issue")
	public ResponseEntity<PolicyResponseDto> issuePolicy(@Valid @RequestBody PolicyIssueRequestDto dto) {

		return ResponseEntity.ok(policyService.issuePolicy(dto));
	}

	@GetMapping("/{id}")
	public ResponseEntity<PolicyResponseDto> getPolicy(@PathVariable Long id) {

		return ResponseEntity.ok(policyService.getPolicyById(id));
	}

	@GetMapping
	public ResponseEntity<Page<PolicyResponseDto>> getAllPolicies(Pageable pageable) {

		return ResponseEntity.ok(policyService.getAllPolicies(pageable));
	}

	@GetMapping("/my")
	public ResponseEntity<Page<PolicyResponseDto>> getMyPolicies(Pageable pageable) {

		return ResponseEntity.ok(policyService.getMyPolicies(pageable));
	}

	@PutMapping("/{id}/cancel")
	public ResponseEntity<PolicyResponseDto> cancelPolicy(@PathVariable Long id) {

		return ResponseEntity.ok(policyService.cancelPolicy(id));
	}
}