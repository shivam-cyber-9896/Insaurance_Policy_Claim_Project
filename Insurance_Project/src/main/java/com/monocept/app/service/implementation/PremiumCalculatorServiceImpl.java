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
        boolean isSmoker = Boolean.TRUE.equals(dto.getIsSmoker());

        // 1. Base Product Risk Rate
        BigDecimal baseRate = getProductBaseRate(productType);

        // 2. Step A: Calculate Base Risk Premium
        BigDecimal totalRiskPremium = coverage.multiply(baseRate);
        BigDecimal riskPremium = (premiumType == PremiumType.ONE_TIME) 
                ? totalRiskPremium.setScale(0, RoundingMode.HALF_UP) 
                : totalRiskPremium.divide(new BigDecimal(duration), 0, RoundingMode.HALF_UP);

        // 3. Step B: Calculate Loading Charges (Age Risk Loading + Smoker Surcharge + Admin/Underwriting Loading)
        BigDecimal ageLoadingPercent = getAgeLoadingPercent(age);
        BigDecimal ageLoadingAmount = riskPremium.multiply(ageLoadingPercent);

        // Smoker loading surcharge applies ONLY to LIFE and HEALTH products (Not applicable for MOTOR or TRAVEL)
        boolean isSmokerApplicable = isSmoker && (productType == ProductType.LIFE || productType == ProductType.HEALTH);
        BigDecimal smokerLoadingPercent = isSmokerApplicable ? getSmokerLoadingPercent(age) : BigDecimal.ZERO;
        BigDecimal smokerLoadingAmount = riskPremium.multiply(smokerLoadingPercent);

        // 10% Admin & Processing Fee
        BigDecimal adminLoadingAmount = riskPremium.multiply(new BigDecimal("0.10"));
        BigDecimal loadingCharges = ageLoadingAmount.add(smokerLoadingAmount).add(adminLoadingAmount).setScale(0, RoundingMode.HALF_UP);

        // 4. Step C: Calculate Discounts (Long-term policy discount + Lump-sum discount)
        BigDecimal durationDiscountPercent = getDurationDiscountPercent(duration);
        BigDecimal termDiscountAmount = riskPremium.multiply(durationDiscountPercent);
        BigDecimal lumpSumDiscountAmount = (premiumType == PremiumType.ONE_TIME) 
                ? riskPremium.multiply(new BigDecimal("0.05")) 
                : BigDecimal.ZERO;

        BigDecimal discounts = termDiscountAmount.add(lumpSumDiscountAmount).setScale(0, RoundingMode.HALF_UP);

        // 5. Step D: Calculate Base Annual Net Premium
        BigDecimal annualNetPremium = riskPremium.add(loadingCharges).subtract(discounts);
        if (annualNetPremium.compareTo(BigDecimal.ZERO) < 0) {
            annualNetPremium = BigDecimal.ZERO;
        }
        annualNetPremium = annualNetPremium.setScale(0, RoundingMode.HALF_UP);

        // 6. Step E: Apply Billing Frequency Installment Factor (Annual 100%, Half-Yearly 55%, Quarterly 27.5%)
        BigDecimal installmentPremium = annualNetPremium;
        if (premiumType == PremiumType.HALF_YEARLY) {
            installmentPremium = annualNetPremium.multiply(new BigDecimal("0.55")).setScale(0, RoundingMode.HALF_UP);
        } else if (premiumType == PremiumType.QUARTERLY) {
            installmentPremium = annualNetPremium.multiply(new BigDecimal("0.275")).setScale(0, RoundingMode.HALF_UP);
        } else if (premiumType == PremiumType.MONTHLY) {
            installmentPremium = annualNetPremium.multiply(new BigDecimal("0.095")).setScale(0, RoundingMode.HALF_UP);
        }


        BigDecimal totalPremiumOverTerm = (premiumType == PremiumType.ONE_TIME)
                ? annualNetPremium
                : annualNetPremium.multiply(new BigDecimal(duration)).setScale(0, RoundingMode.HALF_UP);

        String breakdown = String.format(
                "Formula Applied: Risk Premium (₹%s) + Loading Charges (Age: ₹%s, Smoker +%s%%: ₹%s, Admin: ₹%s) - Discounts (₹%s) = %s Installment Premium (₹%s).",
                riskPremium.toPlainString(),
                ageLoadingAmount.setScale(0, RoundingMode.HALF_UP).toPlainString(),
                smokerLoadingPercent.multiply(new BigDecimal("100")).toPlainString(),
                smokerLoadingAmount.setScale(0, RoundingMode.HALF_UP).toPlainString(),
                adminLoadingAmount.setScale(0, RoundingMode.HALF_UP).toPlainString(),
                discounts.toPlainString(),
                premiumType.name(),
                installmentPremium.toPlainString()
        );


        return PremiumCalculatorResponseDto.builder()
                .coverageAmount(coverage)
                .durationYears(duration)
                .premiumType(premiumType)
                .productType(productType)
                .age(age)
                .isSmoker(isSmoker)
                .smokerRiskFactor(BigDecimal.ONE.add(smokerLoadingPercent))
                .riskPremium(riskPremium)
                .loadingCharges(loadingCharges)
                .discounts(discounts)
                .calculatedPremium(installmentPremium)
                .totalPremiumPaidOverTerm(totalPremiumOverTerm)
                .baseRatePercentage(baseRate.multiply(new BigDecimal("100")))
                .ageRiskFactor(BigDecimal.ONE.add(ageLoadingPercent))
                .breakdownSummary(breakdown)
                .build();
    }

    private BigDecimal getSmokerLoadingPercent(int age) {
        if (age < 30) {
            return new BigDecimal("0.15"); // +15% smoker loading for under 30
        } else if (age < 45) {
            return new BigDecimal("0.25"); // +25% smoker loading for age 30-44
        } else if (age < 60) {
            return new BigDecimal("0.50"); // +50% smoker loading for age 45-59
        } else {
            return new BigDecimal("0.75"); // +75% smoker loading for age 60+
        }
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
}
