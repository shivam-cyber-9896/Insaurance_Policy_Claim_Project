package com.monocept.app.service.implementation;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.monocept.app.dto.*;
import com.monocept.app.enums.AgentSpecialization;
import com.monocept.app.enums.ClaimStatus;
import com.monocept.app.enums.PolicyStatus;
import com.monocept.app.enums.ProductType;
import com.monocept.app.enums.Role;
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
import com.monocept.app.service.EmailService;
import com.monocept.app.service.EmailTempleteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClaimServiceImpl implements ClaimService {

	private final ClaimRepository claimRepository;
	private final PolicyRepository policyRepository;
	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final ClaimStatusHistoryRepository historyRepository;
	private final ModelMapper modelMapper;
	private final CloudinaryServiceImple cloudinaryService;
	private final ClaimDocumentRepository claimDocumentRepository;
	// add to injections at the top
	private final EmailService emailService;
	private final EmailTempleteService emailTemplateService;
	@Override
	@Transactional
	public ClaimResponseDto createClaim(ClaimRequestDto dto, List<MultipartFile> files) throws IOException {

		log.info("Creating claim");

		Policy policy = policyRepository.findByIdWithLock(dto.getPolicyId())
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		/*
		 * if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) { if
		 * (!policy.getCustomer().getUser().getEmail().equals(email)) { throw new
		 * InvalidOperationException("You are not authorized to raise claim for this policy"
		 * ); } }
		 */

		if (policy.getPolicyStatus() != PolicyStatus.ACTIVE) {
			throw new InvalidOperationException("Claim can only be raised for active policy");
		}

		if (dto.getClaimAmount().compareTo(policy.getRemainingCoverage()) > 0) {
			throw new InvalidOperationException("Claim amount exceeds the remaining policy coverage.");
		}

		if (claimRepository.existsByPolicyIdAndClaimAmountAndIncidentDate(policy.getId(), dto.getClaimAmount(), dto.getIncidentDate())) {
			throw new InvalidOperationException("A claim with the same amount and incident date already exists for this policy.");
		}

		policy.setRemainingCoverage(policy.getRemainingCoverage().subtract(dto.getClaimAmount()));
		policyRepository.saveAndFlush(policy);

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
		emailService.sendEmail(
			    savedClaim.getPolicy().getCustomer().getUser().getEmail(),
			    "Claim Submitted - " + savedClaim.getClaimNumber(),
			    emailTemplateService.claimSubmittedTemplate(
			        savedClaim.getPolicy().getCustomer().getUser().getFullName(),
			        savedClaim.getClaimNumber(),
			        savedClaim.getPolicy().getPolicyNumber()
			    )
			);
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
		} else if (dto.getDocuments() != null && !dto.getDocuments().isEmpty()) {
			for (ClaimDocumentRequestDto docDto : dto.getDocuments()) {
				ClaimDocument document = ClaimDocument.builder()
						.claim(savedClaim)
						.documentName(docDto.getDocumentName())
						.documentType(docDto.getDocumentType())
						.documentReference(docDto.getDocumentReference())
						.build();
				claimDocumentRepository.save(document);
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

		// Get logged-in agent
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Specialization check: standard AGENT must match the policy's product type
		if (loggedInUser.getRole() == Role.AGENT) {
			ProductType policyProductType = claim.getPolicy().getPolicyPlan()
					.getInsuranceProduct().getProductType();
			if (loggedInUser.getSpecialization() == null ||
					!loggedInUser.getSpecialization().name().equals(policyProductType.name())) {
				throw new InvalidOperationException(
						"You are not authorized to review this claim. " +
						"Your specialization [" + loggedInUser.getSpecialization() + "] " +
						"does not match the policy type [" + policyProductType + "].");
			}
		}
		// SUPER_AGENT (specialization=SUPER) has no product-type restriction

		// ── Agent suggestion rules ──────────────────────────────────────────
		// Agents can ONLY suggest RECOMMENDED or UNDER_REVIEW — never directly APPROVE or REJECT
		if (dto.getRecommendedStatus() != ClaimStatus.RECOMMENDED && dto.getRecommendedStatus() != ClaimStatus.UNDER_REVIEW) {
			throw new InvalidOperationException(
					"Agents can only suggest RECOMMENDED or UNDER_REVIEW status. " +
					"Final decisions (APPROVED or REJECTED) must be made by the Admin.");
		}

		// Suggested amount must not exceed the original claim amount
		if (dto.getSuggestedAmount() != null) {
			if (dto.getSuggestedAmount().compareTo(claim.getClaimAmount()) > 0) {
				throw new InvalidOperationException(
						"Suggested amount [" + dto.getSuggestedAmount() + "] cannot exceed " +
						"the original claim amount [" + claim.getClaimAmount() + "].");
			}
			claim.setAgentSuggestedAmount(dto.getSuggestedAmount());
		} else {
			// Default: suggest the full claim amount
			claim.setAgentSuggestedAmount(claim.getClaimAmount());
		}
		// ────────────────────────────────────────────────────────────────────


		ClaimStatus oldStatus = claim.getClaimStatus();

		claim.setAgentRemarks(dto.getRemarks());
		claim.setClaimStatus(dto.getRecommendedStatus());

		Policy policy = claim.getPolicy();
		boolean wasAgentAssigned = false;
		if (policy.getAgent() == null) {
			policy.setAgent(loggedInUser);
			policyRepository.save(policy);
			wasAgentAssigned = true;
			log.info("Auto-assigned agent {} to policy {} during claim review", loggedInUser.getId(), policy.getId());
		}

		Claim updatedClaim = claimRepository.save(claim);

		ClaimStatusHistory history = new ClaimStatusHistory();
		history.setClaim(updatedClaim);
		history.setPreviousStatus(oldStatus);
		history.setNewStatus(dto.getRecommendedStatus());
		history.setRemarks(dto.getRemarks());
		history.setUpdatedBy(loggedInUser);
		emailService.sendEmail(
			    updatedClaim.getPolicy().getCustomer().getUser().getEmail(),
			    "Claim Under Review - " + updatedClaim.getClaimNumber(),
			    emailTemplateService.claimStatusUpdatedTemplate(
			        updatedClaim.getPolicy().getCustomer().getUser().getFullName(),
			        updatedClaim.getClaimNumber(),
			        dto.getRecommendedStatus().toString(),
			        dto.getRemarks()
			    )
			);
		historyRepository.save(history);

		if (wasAgentAssigned) {
			try {
				emailService.sendEmail(loggedInUser.getEmail(), "Policy Auto-Assigned - " + policy.getPolicyNumber(),
						emailTemplateService.agentAssignedTemplate(loggedInUser.getFullName(),
								policy.getCustomer().getUser().getFullName(), policy.getPolicyNumber()));
			} catch (Exception e) {
				log.error("Failed to send auto-assigned email to agent: ", e);
			}
		}

		return convertToDto(updatedClaim);
	}


	@Override
	@Transactional
	public ClaimResponseDto finalDecision(Long claimId, ClaimFinalDecisionRequestDto dto) {

		Claim claim = findClaimById(claimId);

		if (claim.getClaimStatus() == ClaimStatus.SUBMITTED) {
			throw new InvalidOperationException("Admin cannot make a final decision on a claim until it has been reviewed by an agent.");
		}

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {

			throw new InvalidOperationException("Approved or rejected claims cannot be modified.");
		}

		ClaimStatus oldStatus = claim.getClaimStatus();

		claim.setAdminRemarks(dto.getRemarks());

		claim.setClaimStatus(dto.getFinalDecisionStatus());

		if (dto.getFinalDecisionStatus() == ClaimStatus.REJECTED) {
			Policy policy = policyRepository.findByIdWithLock(claim.getPolicy().getId())
					.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
			policy.setRemainingCoverage(policy.getRemainingCoverage().add(claim.getClaimAmount()));
			policyRepository.save(policy);
		} else if (dto.getFinalDecisionStatus() == ClaimStatus.APPROVED) {
			BigDecimal suggested = claim.getAgentSuggestedAmount();
			if (suggested != null && suggested.compareTo(claim.getClaimAmount()) < 0) {
				BigDecimal refund = claim.getClaimAmount().subtract(suggested);
				Policy policy = policyRepository.findByIdWithLock(claim.getPolicy().getId())
						.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
				policy.setRemainingCoverage(policy.getRemainingCoverage().add(refund));
				policyRepository.save(policy);
			}
		}

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
		emailService.sendEmail(
			    updatedClaim.getPolicy().getCustomer().getUser().getEmail(),
			    "Claim Decision - " + updatedClaim.getClaimNumber(),
			    emailTemplateService.claimStatusUpdatedTemplate(
			        updatedClaim.getPolicy().getCustomer().getUser().getFullName(),
			        updatedClaim.getClaimNumber(),
			        dto.getFinalDecisionStatus().toString(),
			        dto.getRemarks()
			    )
			);
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

	    List<ClaimDocumentResponseDto> documentDtos = claimDocumentRepository.findByClaimId(claim.getId())
	        .stream()
	        .map(doc -> ClaimDocumentResponseDto.builder()
	            .id(doc.getId())
	            .documentName(doc.getDocumentName())
	            .documentType(doc.getDocumentType())
	            .documentReference(doc.getDocumentReference())
	            .build())
	        .collect(Collectors.toList());

	    User agent = claim.getPolicy().getAgent();
	    Long agentId = agent != null ? agent.getId() : null;
	    String agentName = agent != null ? agent.getFullName() : null;
	    String agentEmail = agent != null ? agent.getEmail() : null;

	    return ClaimResponseDto.builder()
	        .id(claim.getId())
	        .claimNumber(claim.getClaimNumber())
	        .policyNumber(claim.getPolicy().getPolicyNumber())
	        .customerName(claim.getPolicy().getCustomer().getUser().getFullName())
	        .claimAmount(claim.getClaimAmount())
	        .claimReason(claim.getClaimReason())
	        .incidentDate(claim.getIncidentDate())
	        .claimStatus(claim.getClaimStatus())
	        .agentRemarks(claim.getAgentRemarks())
	        .agentSuggestedAmount(claim.getAgentSuggestedAmount())
	        .adminRemarks(claim.getAdminRemarks())
	        .createdAt(claim.getCreatedAt())
	        .updatedAt(claim.getUpdatedAt())
	        .documents(documentDtos)
	        .agentId(agentId)
	        .agentName(agentName)
	        .agentEmail(agentEmail)
	        .productType(claim.getPolicy().getPolicyPlan().getInsuranceProduct().getProductType())
	        .build();
	}

	@Override
	@Transactional
	public List<ClaimResponseDto> superRuleApproveClaims(ProductType productType, BigDecimal amountThreshold) {

		log.info("Super Rule: Auto-approving RECOMMENDED claims for productType={} with amount<={}",
				productType, amountThreshold);

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User admin = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

		List<Claim> eligibleClaims = claimRepository.findRecommendedClaimsForSuperRule(
				ClaimStatus.RECOMMENDED, productType, amountThreshold);

		if (eligibleClaims.isEmpty()) {
			log.info("Super Rule: No eligible claims found.");
			return java.util.Collections.emptyList();
		}

		return eligibleClaims.stream().map(claim -> {
			ClaimStatus oldStatus = claim.getClaimStatus();
			claim.setClaimStatus(ClaimStatus.APPROVED);
			claim.setAdminRemarks("Auto-approved by Super Rule for " + productType + " policies with amount <= " + amountThreshold);

			BigDecimal suggested = claim.getAgentSuggestedAmount();
			if (suggested != null && suggested.compareTo(claim.getClaimAmount()) < 0) {
				BigDecimal refund = claim.getClaimAmount().subtract(suggested);
				try {
					Policy policy = policyRepository.findByIdWithLock(claim.getPolicy().getId())
							.orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
					policy.setRemainingCoverage(policy.getRemainingCoverage().add(refund));
					policyRepository.save(policy);
				} catch (Exception ex) {
					log.error("Failed to refund remaining coverage difference during auto-approval: ", ex);
				}
			}

			Claim saved = claimRepository.save(claim);

			// Record history
			ClaimStatusHistory history = new ClaimStatusHistory();
			history.setClaim(saved);
			history.setPreviousStatus(oldStatus);
			history.setNewStatus(ClaimStatus.APPROVED);
			history.setRemarks("Auto-approved via Super Rule");
			history.setUpdatedBy(admin);
			historyRepository.save(history);

			// Notify customer
			emailService.sendEmail(
					saved.getPolicy().getCustomer().getUser().getEmail(),
					"Claim Approved - " + saved.getClaimNumber(),
					emailTemplateService.claimStatusUpdatedTemplate(
							saved.getPolicy().getCustomer().getUser().getFullName(),
							saved.getClaimNumber(),
							ClaimStatus.APPROVED.toString(),
							"Your claim has been auto-approved under the Super Rule."));

			return convertToDto(saved);
		}).collect(Collectors.toList());
	}
}