package com.monocept.app.service;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.monocept.app.dto.ProductRequestDto;
import com.monocept.app.dto.ProductResponseDto;

public interface InsuranceProductService {
	ProductResponseDto createProduct(ProductRequestDto dto);

    ProductResponseDto updateProduct(Long id, ProductRequestDto dto);

    ProductResponseDto deactivateProduct(Long id);

    ProductResponseDto getProductById(Long id);

    Page<ProductResponseDto> getAllProducts(Pageable pageable);
}
