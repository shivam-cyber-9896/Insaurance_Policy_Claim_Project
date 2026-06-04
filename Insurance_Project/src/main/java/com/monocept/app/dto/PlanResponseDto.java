package com.monocept.app.dto;


import com.monocept.app.enums.PremiumType;
import com.monocept.app.enums.ProductType;

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
