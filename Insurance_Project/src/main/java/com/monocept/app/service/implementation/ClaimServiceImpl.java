package com.monocept.app.service.implementation;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.monocept.app.dto.*;
import com.monocept.app.enums.ClaimStatus;
import com.monocept.app.enums.PolicyStatus;
import com.monocept.app.exception.InvalidOperationException;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.Claim;
import com.monocept.app.model.ClaimDocument;
import com.monocept.app.model.Policy;
import com.monocept.app.model.User;
import com.monocept.app.model.Customer;
import com.monocept.app.model.ClaimStatusHistory;
import com.monocept.app.repository.ClaimDocumentRepository;
import com.monocept.app.repository.ClaimRepository;
import com.monocept.app.repository.PolicyRepository;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.repository.CustomerRepository;
import com.monocept.app.repository.ClaimStatusHistoryRepository;
import com.monocept.app.service.ClaimService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

	private final ClaimRepository claimRepository;
	private final PolicyRepository policyRepository;
	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final ClaimStatusHistoryRepository historyRepository;
	private final ModelMapper modelMapper;
	private final CloudinaryServiceImple cloudinaryService;
	private final ClaimDocumentRepository claimDocumentRepository;

	@Override
	public ClaimResponseDto createClaim(ClaimRequestDto dto, List<MultipartFile> files) throws IOException {

		log.info("Creating claim");

		Policy policy = policyRepository.findById(dto.getPolicyId())
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) {
			if (!policy.getCustomer().getUser().getEmail().equals(email)) {
				throw new InvalidOperationException("You are not authorized to raise claim for this policy");
			}
		}

		if (policy.getPolicyStatus() != PolicyStatus.ACTIVE) {
			throw new InvalidOperationException("Claim can only be raised for active policy");
		}

		if (dto.getClaimAmount().compareTo(policy.getPolicyPlan().getCoverageAmount()) > 0) {
			throw new InvalidOperationException("Claim amount cannot exceed policy coverage amount of "
					+ policy.getPolicyPlan().getCoverageAmount());
		}

		Claim claim = modelMapper.map(dto, Claim.class);
		claim.setPolicy(policy);

		String claimNumber;
		do {
			claimNumber = "CLM-"
					+ java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
					+ java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
		} while (claimRepository.existsByClaimNumber(claimNumber));

		claim.setClaimNumber(claimNumber);
		claim.setClaimStatus(ClaimStatus.SUBMITTED);

		Claim savedClaim = claimRepository.save(claim);

		// upload documents to Cloudinary if provided
		if (files != null && !files.isEmpty()) {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					String originalFilename = file.getOriginalFilename();
					String extension = (originalFilename != null && originalFilename.contains("."))
							? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toUpperCase()
							: "PDF";

					String fileUrl = cloudinaryService.uploadFile(file, "claims/" + savedClaim.getId());

					ClaimDocument document = ClaimDocument.builder().claim(savedClaim)
							.documentName(originalFilename != null ? originalFilename : "document")
							.documentType(extension).documentReference(fileUrl).build();

					claimDocumentRepository.save(document);
				}
			}
		}

		// Record in history
		ClaimStatusHistory history = new ClaimStatusHistory();
		history.setClaim(savedClaim);
		history.setPreviousStatus(null);
		history.setNewStatus(ClaimStatus.SUBMITTED);
		history.setRemarks("Claim submitted");
		history.setUpdatedBy(loggedInUser);
		historyRepository.save(history);

		log.info("Claim created successfully: {}", savedClaim.getClaimNumber());

		return convertToDto(savedClaim);
	}

	@Override
	@Transactional
	public ClaimResponseDto reviewClaim(Long claimId, ClaimReviewRequestDto dto) {

		Claim claim = findClaimById(claimId);

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {

			throw new InvalidOperationException("Approved or rejected claims cannot be modified.");
		}

		ClaimStatus oldStatus = claim.getClaimStatus();

		claim.setAgentRemarks(dto.getRemarks());

		claim.setClaimStatus(dto.getRecommendedStatus());

		Claim updatedClaim = claimRepository.save(claim);

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		ClaimStatusHistory history = new ClaimStatusHistory();

		history.setClaim(updatedClaim);
		history.setPreviousStatus(oldStatus);
		history.setNewStatus(dto.getRecommendedStatus());
		history.setRemarks(dto.getRemarks());
		history.setUpdatedBy(loggedInUser);

		historyRepository.save(history);

		return convertToDto(updatedClaim);
	}

	@Override
	@Transactional
	public ClaimResponseDto finalDecision(Long claimId, ClaimFinalDecisionRequestDto dto) {

		Claim claim = findClaimById(claimId);

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {

			throw new InvalidOperationException("Approved or rejected claims cannot be modified.");
		}

		ClaimStatus oldStatus = claim.getClaimStatus();

		claim.setAdminRemarks(dto.getRemarks());

		claim.setClaimStatus(dto.getFinalDecisionStatus());

		Claim updatedClaim = claimRepository.save(claim);

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		ClaimStatusHistory history = new ClaimStatusHistory();

		history.setClaim(updatedClaim);
		history.setPreviousStatus(oldStatus);
		history.setNewStatus(dto.getFinalDecisionStatus());
		history.setRemarks(dto.getRemarks());
		history.setUpdatedBy(loggedInUser);

		historyRepository.save(history);

		return convertToDto(updatedClaim);
	}

	@Override
	public ClaimResponseDto getClaimById(Long id) {

		Claim claim = findClaimById(id);

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) {
			if (!claim.getPolicy().getCustomer().getUser().getEmail().equals(email)) {
				throw new com.monocept.app.exception.InvalidOperationException(
						"You are not authorized to view this claim");
			}
		}

		return convertToDto(claim);
	}

	@Override
	public Page<ClaimResponseDto> getAllClaims(Pageable pageable) {

		return claimRepository.findAll(pageable).map(this::convertToDto);
	}

	@Override
	public Page<ClaimResponseDto> getMyClaims(Pageable pageable) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Customer customer = customerRepository.findByUser(user)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
		return claimRepository.findByPolicyCustomer(customer, pageable).map(this::convertToDto);
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