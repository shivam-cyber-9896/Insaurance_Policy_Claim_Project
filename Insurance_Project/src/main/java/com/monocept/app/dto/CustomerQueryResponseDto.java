package com.monocept.app.dto;

import java.time.LocalDateTime;
import com.monocept.app.enums.QueryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerQueryResponseDto {

    private Long id;
    private String fullName;
    private String email;
    private String subject;
    private String message;
    private QueryStatus status;
    private String response;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
}
