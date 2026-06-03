package com.example.app.dto;

import com.example.app.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String productName;
    private ProductType productType;
    private String description;
    private boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
