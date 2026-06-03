package com.example.app.dto;

import com.example.app.enums.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponseDto {
    private Long id;
    private String claimNumber;
    private String policyNumber;
    private String customerName;
    private BigDecimal claimAmount;
    private String claimReason;
    private LocalDate incidentDate;
    private ClaimStatus claimStatus;
    private String agentRemarks;
    private String adminRemarks;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
