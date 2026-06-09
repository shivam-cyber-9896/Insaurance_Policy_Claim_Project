package com.monocept.app.dto;


import java.math.BigDecimal;

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
	private BigDecimal coverageAmount;
	private BigDecimal premiumAmount;
	private PremiumType premiumType;
	private Integer durationYears;
	private String termsAndConditions;
	private boolean active;
}
