package com.monocept.app.dto;

import com.monocept.app.enums.QueryStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerQueryReplyDto {

    @NotBlank(message = "Response message is required")
    private String response;

    private QueryStatus status;
}
