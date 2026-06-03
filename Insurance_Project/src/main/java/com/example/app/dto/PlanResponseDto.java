package com.example.app.dto;

import com.example.app.enums.PremiumType;
import com.example.app.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponseDto {
    private Long id;
    private Long productId;
    private String productName;
    private ProductType productType;
    private String planName;
    private Double coverageAmount;
    private Double premiumAmount;
    private PremiumType premiumType;
    private Integer duration;
    private String termsAndConditions;
    private boolean active;
}
