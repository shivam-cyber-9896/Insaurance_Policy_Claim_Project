package com.example.app.dto;

import com.example.app.enums.ClaimStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReviewRequestDto {

    @NotNull(message = "Recommended status is required")
    private ClaimStatus recommendedStatus;

    @NotBlank(message = "Remarks are required")
    private String remarks;
}
