package com.monocept.app.service.implementation;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.ClaimHistoryResponseDto;
import com.monocept.app.model.Claim;
import com.monocept.app.model.User;
import com.monocept.app.repository.ClaimRepository;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.repository.ClaimStatusHistoryRepository;
import com.monocept.app.service.ClaimHistoryService;
import com.monocept.app.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimHistoryServiceImpl implements ClaimHistoryService {

	private final ClaimStatusHistoryRepository historyRepository;
	private final ClaimRepository claimRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;

	@Override
	public List<ClaimHistoryResponseDto> getHistoryByClaim(Long claimId) {

		log.info("Fetching claim history for claim: {}", claimId);

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) {
			if (!claim.getPolicy().getCustomer().getUser().getEmail().equals(email)) {
				throw new com.monocept.app.exception.InvalidOperationException("You are not authorized to view the history of this claim");
			}
		}

		return historyRepository.findByClaimOrderByChangedAtDesc(claim).stream()
				.map(history -> modelMapper.map(history, ClaimHistoryResponseDto.class)).toList();
	}
}