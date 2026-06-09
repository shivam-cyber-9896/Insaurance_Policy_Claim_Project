package com.monocept.app.dto;

import com.monocept.app.enums.ProductType;
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
   
    private String productName;
    private ProductType productType;
    private String description;
    private boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
