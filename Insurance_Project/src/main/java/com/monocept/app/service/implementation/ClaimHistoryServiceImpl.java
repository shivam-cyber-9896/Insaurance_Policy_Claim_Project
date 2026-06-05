package com.monocept.app.service.implementation;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.ClaimHistoryResponseDto;
import com.monocept.app.repository.ClaimStatusHistoryRepository;
import com.monocept.app.service.ClaimHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimHistoryServiceImpl implements ClaimHistoryService {

	private final ClaimStatusHistoryRepository historyRepository;

	private final ModelMapper modelMapper;

	@Override
	public List<ClaimHistoryResponseDto> getHistoryByClaim(Long claimId) {

		log.info("Fetching claim history for claim: {}", claimId);

		return historyRepository.findById(claimId).stream()
				.map(history -> modelMapper.map(history, ClaimHistoryResponseDto.class)).toList();
	}
}