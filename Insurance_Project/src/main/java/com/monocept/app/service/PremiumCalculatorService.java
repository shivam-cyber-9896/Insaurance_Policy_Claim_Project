package com.monocept.app.service;

import com.monocept.app.dto.PremiumCalculatorRequestDto;
import com.monocept.app.dto.PremiumCalculatorResponseDto;

public interface PremiumCalculatorService {

    PremiumCalculatorResponseDto calculatePremium(PremiumCalculatorRequestDto dto);
}
