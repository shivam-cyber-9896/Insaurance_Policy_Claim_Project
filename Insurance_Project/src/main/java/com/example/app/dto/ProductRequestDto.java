package com.example.app.dto;

import com.example.app.enums.ProductType;
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
public class ProductRequestDto {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotBlank(message = "Product description is required")
    private String description;

    @Builder.Default
    private boolean active = true;
}
