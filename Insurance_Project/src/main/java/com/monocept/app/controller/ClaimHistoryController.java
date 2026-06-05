package com.monocept.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monocept.app.dto.ClaimHistoryResponseDto;
import com.monocept.app.service.ClaimHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claim-history")
@RequiredArgsConstructor
public class ClaimHistoryController {

	private final ClaimHistoryService claimHistoryService;

	@GetMapping("/{claimId}")
	public ResponseEntity<List<ClaimHistoryResponseDto>> getHistory(@PathVariable Long claimId) {

		return ResponseEntity.ok(claimHistoryService.getHistoryByClaim(claimId));
	}
}