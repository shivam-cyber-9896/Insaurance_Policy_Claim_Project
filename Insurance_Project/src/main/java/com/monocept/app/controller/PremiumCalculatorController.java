package com.monocept.app.controller;

import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.monocept.app.dto.PremiumCalculatorRequestDto;
import com.monocept.app.dto.PremiumCalculatorResponseDto;
import com.monocept.app.enums.PremiumType;
import com.monocept.app.enums.ProductType;
import com.monocept.app.service.PremiumCalculatorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/calculator")
@RequiredArgsConstructor
public class PremiumCalculatorController {

    private final PremiumCalculatorService calculatorService;

    @PostMapping("/premium")
    public ResponseEntity<PremiumCalculatorResponseDto> calculatePremiumPost(@Valid @RequestBody PremiumCalculatorRequestDto dto) {
        return ResponseEntity.ok(calculatorService.calculatePremium(dto));
    }

    @GetMapping("/premium")
    public ResponseEntity<PremiumCalculatorResponseDto> calculatePremiumGet(
            @RequestParam BigDecimal coverageAmount,
            @RequestParam(defaultValue = "1") Integer durationYears,
            @RequestParam(defaultValue = "ANNUAL") PremiumType premiumType,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(defaultValue = "30") Integer age) {

        PremiumCalculatorRequestDto dto = PremiumCalculatorRequestDto.builder()
                .coverageAmount(coverageAmount)
                .durationYears(durationYears)
                .premiumType(premiumType)
                .productType(productType)
                .age(age)
                .build();

        return ResponseEntity.ok(calculatorService.calculatePremium(dto));
    }
}
