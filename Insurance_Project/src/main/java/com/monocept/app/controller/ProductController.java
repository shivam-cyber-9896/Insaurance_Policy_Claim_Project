package com.monocept.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monocept.app.dto.ProductRequestDto;
import com.monocept.app.dto.ProductResponseDto;
import com.monocept.app.service.InsuranceProductService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final InsuranceProductService productService;

	@PostMapping
	public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto dto) {

		return ResponseEntity.ok(productService.createProduct(dto));
	}

	@GetMapping
	public ResponseEntity<Page<ProductResponseDto>> getAllProducts(Pageable pageable) {

		return ResponseEntity.ok(productService.getAllProducts(pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {

		return ResponseEntity.ok(productService.getProductById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDto dto) {

		return ResponseEntity.ok(productService.updateProduct(id, dto));
	}

	@PutMapping("/{id}/deactivate")
	public ResponseEntity<ProductResponseDto> deactivateProduct(@PathVariable Long id) {

		return ResponseEntity.ok(productService.deactivateProduct(id));
	}
}