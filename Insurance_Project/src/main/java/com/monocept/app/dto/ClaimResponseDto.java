package com.monocept.app.dto;

import com.monocept.app.enums.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponseDto {
	private String claimNumber;
    private String policyNumber;
    private String customerName;
    private BigDecimal claimAmount;
    private String claimReason;
    private LocalDate incidentDate;
    private ClaimStatus claimStatus;
    private String agentRemarks;
    private String adminRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ClaimDocumentResponseDto> documents;
}
