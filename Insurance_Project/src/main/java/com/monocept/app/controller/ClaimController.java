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

import com.monocept.app.dto.ClaimFinalDecisionRequestDto;
import com.monocept.app.dto.ClaimRequestDto;
import com.monocept.app.dto.ClaimResponseDto;
import com.monocept.app.dto.ClaimReviewRequestDto;
import com.monocept.app.service.ClaimService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

	private final ClaimService claimService;

 	@PostMapping
	public ResponseEntity<ClaimResponseDto> createClaim(@Valid @RequestBody ClaimRequestDto dto) {

		return ResponseEntity.ok(claimService.createClaim(dto));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ClaimResponseDto> getClaimById(@PathVariable Long id) {

		return ResponseEntity.ok(claimService.getClaimById(id));
	}

	@GetMapping
	public ResponseEntity<Page<ClaimResponseDto>> getAllClaims(Pageable pageable) {

		return ResponseEntity.ok(claimService.getAllClaims(pageable));
	}

	@GetMapping("/my")
	public ResponseEntity<Page<ClaimResponseDto>> getMyClaims(Pageable pageable) {

		return ResponseEntity.ok(claimService.getMyClaims(pageable));
	}

	@PutMapping("/{id}/review")
	public ResponseEntity<ClaimResponseDto> reviewClaim(@PathVariable Long id, @RequestBody ClaimReviewRequestDto dto) {

		return ResponseEntity.ok(claimService.reviewClaim(id, dto));
	}

	@PutMapping("/{id}/decision")
	public ResponseEntity<ClaimResponseDto> finalDecision(@PathVariable Long id,
			@RequestBody ClaimFinalDecisionRequestDto dto) {

		return ResponseEntity.ok(claimService.finalDecision(id, dto));
	}
}