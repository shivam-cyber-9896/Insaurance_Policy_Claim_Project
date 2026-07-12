package com.monocept.app.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.monocept.app.dto.ClaimFinalDecisionRequestDto;
import com.monocept.app.dto.ClaimRequestDto;
import com.monocept.app.dto.ClaimResponseDto;
import com.monocept.app.dto.ClaimReviewRequestDto;
import com.monocept.app.enums.ProductType;

public interface ClaimService {

	ClaimResponseDto createClaim(ClaimRequestDto dto, List<MultipartFile> files) throws IOException;

	ClaimResponseDto reviewClaim(Long claimId, ClaimReviewRequestDto dto);

	ClaimResponseDto finalDecision(Long claimId, ClaimFinalDecisionRequestDto dto);

	ClaimResponseDto getClaimById(Long id);

	Page<ClaimResponseDto> getAllClaims(Pageable pageable);

	Page<ClaimResponseDto> getMyClaims(Pageable pageable);

	/**
	 * Super Rule: Admin triggers bulk auto-approval of all RECOMMENDED claims
	 * of a given product type where claimAmount <= amountThreshold.
	 * Returns list of approved claim responses.
	 */
	List<ClaimResponseDto> superRuleApproveClaims(ProductType productType, BigDecimal amountThreshold);
}