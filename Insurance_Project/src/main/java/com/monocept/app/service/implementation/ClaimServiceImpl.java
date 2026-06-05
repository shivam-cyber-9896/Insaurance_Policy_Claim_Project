package com.monocept.app.service.implementation;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.*;
import com.monocept.app.enums.ClaimStatus;
import com.monocept.app.enums.PolicyStatus;
import com.monocept.app.exception.InvalidOperationException;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.Claim;
import com.monocept.app.model.Policy;
import com.monocept.app.repository.ClaimRepository;
import com.monocept.app.repository.PolicyRepository;
import com.monocept.app.service.ClaimService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

	private final ClaimRepository claimRepository;
	private final PolicyRepository policyRepository;
	private final ModelMapper modelMapper;

	@Override
	public ClaimResponseDto createClaim(ClaimRequestDto dto) {

		log.info("Creating claim");

		Policy policy = policyRepository.findById(dto.getPolicyId())
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

		if (policy.getPolicyStatus() != PolicyStatus.ACTIVE) {

			throw new InvalidOperationException("Claim can only be raised for active policy");
		}

		Claim claim = modelMapper.map(dto, Claim.class);

		claim.setPolicy(policy);

		String claimNumber;

		do {
			claimNumber = "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		} while (claimRepository.existsByClaimNumber(claimNumber));

		claim.setClaimNumber(claimNumber);

		claim.setClaimStatus(ClaimStatus.SUBMITTED);

		Claim savedClaim = claimRepository.save(claim);

		log.info("Claim created successfully: {}", savedClaim.getClaimNumber());

		return convertToDto(savedClaim);
	}

	@Override
	public ClaimResponseDto reviewClaim(Long claimId, ClaimReviewRequestDto dto) {

		Claim claim = findClaimById(claimId);

		claim.setAgentRemarks(dto.getRemarks());

		claim.setClaimStatus(dto.getRecommendedStatus());

		Claim updatedClaim = claimRepository.save(claim);

		return convertToDto(updatedClaim);
	}

	@Override
	public ClaimResponseDto finalDecision(Long claimId, ClaimFinalDecisionRequestDto dto) {

		Claim claim = findClaimById(claimId);

		claim.setAdminRemarks(dto.getRemarks());

		claim.setClaimStatus(dto.getFinalDecisionStatus());

		Claim updatedClaim = claimRepository.save(claim);

		return convertToDto(updatedClaim);
	}

	@Override
	public ClaimResponseDto getClaimById(Long id) {

		return convertToDto(findClaimById(id));
	}

	@Override
	public Page<ClaimResponseDto> getAllClaims(Pageable pageable) {

		return claimRepository.findAll(pageable).map(this::convertToDto);
	}

	private Claim findClaimById(Long id) {

		return claimRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));
	}

	private ClaimResponseDto convertToDto(Claim claim) {

		ClaimResponseDto dto = modelMapper.map(claim, ClaimResponseDto.class);

		dto.setPolicyNumber(claim.getPolicy().getPolicyNumber());

		dto.setCustomerName(claim.getPolicy().getCustomer().getUser().getFullName());

		return dto;
	}

	

	

}