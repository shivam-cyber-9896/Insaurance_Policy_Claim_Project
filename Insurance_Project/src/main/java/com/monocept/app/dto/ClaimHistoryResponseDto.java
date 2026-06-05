package com.monocept.app.dto;

import java.time.LocalDateTime;

import com.monocept.app.enums.ClaimStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimHistoryResponseDto {

    private Long id;

    private ClaimStatus previousStatus;

    private ClaimStatus newStatus;

    private String remarks;

    private String updatedBy;

    private LocalDateTime updatedDate;
}