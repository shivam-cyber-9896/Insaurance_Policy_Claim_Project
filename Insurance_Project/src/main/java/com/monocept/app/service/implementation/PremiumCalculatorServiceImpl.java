package com.monocept.app.service.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.PremiumCalculatorRequestDto;
import com.monocept.app.dto.PremiumCalculatorResponseDto;
import com.monocept.app.enums.PremiumType;
import com.monocept.app.enums.ProductType;
import com.monocept.app.service.PremiumCalculatorService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PremiumCalculatorServiceImpl implements PremiumCalculatorService {

    @Override
    public PremiumCalculatorResponseDto calculatePremium(PremiumCalculatorRequestDto dto) {
        log.info("Calculating policy premium using formula Premium = (Risk Premium) + (Loading Charges) - (Discounts)");

        BigDecimal coverage = dto.getCoverageAmount();
        int duration = dto.getDurationYears() != null && dto.getDurationYears() > 0 ? dto.getDurationYears() : 1;
        ProductType productType = dto.getProductType() != null ? dto.getProductType() : ProductType.LIFE;
        PremiumType premiumType = dto.getPremiumType() != null ? dto.getPremiumType() : PremiumType.ANNUAL;
        int age = dto.getAge() != null ? dto.getAge() : 30;
        boolean isSmoker = dto.getIsSmoker() != null ? dto.getIsSmoker() : false;

        // 1. Base Product Risk Rate
        BigDecimal baseRate = getProductBaseRate(productType);

        // 2. Step A: Calculate Base Risk Premium
        BigDecimal totalRiskPremium = coverage.multiply(baseRate);
        BigDecimal riskPremium = (premiumType == PremiumType.ONE_TIME) 
                ? totalRiskPremium.setScale(2, RoundingMode.HALF_UP) 
                : totalRiskPremium.divide(new BigDecimal(duration), 2, RoundingMode.HALF_UP);

        // 3. Step B: Calculate Loading Charges (Age Risk Loading + Smoker Loading + Frequency Interest + Admin/Underwriting Loading)
        BigDecimal ageLoadingPercent = getAgeLoadingPercent(age);
        BigDecimal ageLoadingAmount = riskPremium.multiply(ageLoadingPercent);
        
        BigDecimal smokerLoadingAmount = isSmoker ? riskPremium.multiply(new BigDecimal("0.15")) : BigDecimal.ZERO;
        
        BigDecimal frequencyInterestPercent = getFrequencyInterestPercent(premiumType);
        BigDecimal frequencyInterestAmount = riskPremium.multiply(frequencyInterestPercent);
        
        BigDecimal adminLoadingAmount = riskPremium.multiply(new BigDecimal("0.05")); // 5% admin & processing loading
        
        BigDecimal loadingCharges = ageLoadingAmount
                .add(smokerLoadingAmount)
                .add(frequencyInterestAmount)
                .add(adminLoadingAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Step C: Calculate Discounts (Long-term policy discount + Lump-sum discount)
        BigDecimal durationDiscountPercent = getDurationDiscountPercent(duration);
        BigDecimal termDiscountAmount = riskPremium.multiply(durationDiscountPercent);
        BigDecimal lumpSumDiscountAmount = (premiumType == PremiumType.ONE_TIME) 
                ? riskPremium.multiply(new BigDecimal("0.05")) 
                : BigDecimal.ZERO;

        BigDecimal discounts = termDiscountAmount.add(lumpSumDiscountAmount).setScale(2, RoundingMode.HALF_UP);

        // 5. Step D: Formula: Final Premium = Risk Premium + Loading Charges - Discounts
        BigDecimal finalCalculatedPremium = riskPremium.add(loadingCharges).subtract(discounts);
        if (finalCalculatedPremium.compareTo(BigDecimal.ZERO) < 0) {
            finalCalculatedPremium = BigDecimal.ZERO;
        }
        finalCalculatedPremium = finalCalculatedPremium.setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPremiumOverTerm = (premiumType == PremiumType.ONE_TIME)
                ? finalCalculatedPremium
                : finalCalculatedPremium.multiply(new BigDecimal(duration)).setScale(2, RoundingMode.HALF_UP);

        String breakdown = String.format(
                "Formula: Risk Premium (₹%s) + Age Load (₹%s) + Smoker Load (₹%s) + Freq Interest (₹%s) + Admin (₹%s) - Discounts (₹%s) = Final Net Premium (₹%s / %s).",
                riskPremium.toPlainString(),
                ageLoadingAmount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                smokerLoadingAmount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                frequencyInterestAmount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                adminLoadingAmount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                discounts.toPlainString(),
                finalCalculatedPremium.toPlainString(),
                premiumType == PremiumType.ONE_TIME ? "one-time" : "installment"
        );

        return PremiumCalculatorResponseDto.builder()
                .coverageAmount(coverage)
                .durationYears(duration)
                .premiumType(premiumType)
                .productType(productType)
                .age(age)
                .riskPremium(riskPremium)
                .loadingCharges(loadingCharges)
                .discounts(discounts)
                .calculatedPremium(finalCalculatedPremium)
                .totalPremiumPaidOverTerm(totalPremiumOverTerm)
                .baseRatePercentage(baseRate.multiply(new BigDecimal("100")))
                .ageRiskFactor(BigDecimal.ONE.add(ageLoadingPercent))
                .breakdownSummary(breakdown)
                .build();
    }

    private BigDecimal getProductBaseRate(ProductType productType) {
        if (productType == null) return new BigDecimal("0.025");
        switch (productType) {
            case LIFE:
                return new BigDecimal("0.025"); // 2.5%
            case HEALTH:
                return new BigDecimal("0.035"); // 3.5%
            case MOTOR:
                return new BigDecimal("0.040"); // 4.0%
            case TRAVEL:
                return new BigDecimal("0.010"); // 1.0%
            default:
                return new BigDecimal("0.025");
        }
    }

    private BigDecimal getAgeLoadingPercent(int age) {
        if (age < 30) {
            return new BigDecimal("0.00"); // 0% loading
        } else if (age < 45) {
            return new BigDecimal("0.15"); // +15% loading
        } else if (age < 60) {
            return new BigDecimal("0.35"); // +35% loading
        } else {
            return new BigDecimal("0.60"); // +60% loading
        }
    }

    private BigDecimal getDurationDiscountPercent(int durationYears) {
        if (durationYears >= 10) {
            return new BigDecimal("0.10"); // 10% discount for >= 10 years
        } else if (durationYears >= 5) {
            return new BigDecimal("0.05"); // 5% discount for >= 5 years
        } else {
            return new BigDecimal("0.00");
        }
    }

    private BigDecimal getFrequencyInterestPercent(PremiumType premiumType) {
        if (premiumType == null) return BigDecimal.ZERO;
        switch (premiumType) {
            case MONTHLY: return new BigDecimal("0.10"); // +10%
            case QUARTERLY: return new BigDecimal("0.08"); // +8%
            case HALF_YEARLY: return new BigDecimal("0.05"); // +5%
            default: return BigDecimal.ZERO; // ANNUAL or ONE_TIME
        }
    }
}
