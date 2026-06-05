package com.monocept.app.service;

import java.util.List;

import com.monocept.app.dto.ClaimHistoryResponseDto;

public interface  ClaimHistoryService {

    List<ClaimHistoryResponseDto> getHistoryByClaim(
            Long claimId);
}
