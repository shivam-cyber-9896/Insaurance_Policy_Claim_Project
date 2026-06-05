package com.monocept.app.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.ProductRequestDto;
import com.monocept.app.dto.ProductResponseDto;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.exception.CustomExceptions.DuplicateResourceException;
import com.monocept.app.model.InsuranceProduct;
import com.monocept.app.repository.InsuranceProductRepository;
import com.monocept.app.service.InsuranceProductService;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceProductServiceImpl implements InsuranceProductService {

	private final InsuranceProductRepository productRepository;
	private final ModelMapper modelMapper;

	@Override
	public ProductResponseDto createProduct(ProductRequestDto dto) {

		log.info("Creating insurance product: {}", dto.getProductName());

		if (productRepository.existsByProductName(dto.getProductName())) {

			log.warn("Duplicate product name: {}", dto.getProductName());

			throw new DuplicateResourceException("Product name already exists");
		}

		InsuranceProduct product = modelMapper.map(dto, InsuranceProduct.class);

		InsuranceProduct savedProduct = productRepository.save(product);

		log.info("Insurance product created successfully with id: {}", savedProduct.getId());

		return modelMapper.map(savedProduct, ProductResponseDto.class);
	}

	@Override
	public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {

		log.info("Updating product with id: {}", id);

		InsuranceProduct product = findProductById(id);

		if (productRepository.existsByProductNameAndIdNot(dto.getProductName(), id)) {

			log.warn("Duplicate product name found: {}", dto.getProductName());

			throw new DuplicateResourceException("Product name already exists");
		}

		product.setProductName(dto.getProductName());
		product.setProductType(dto.getProductType());
		product.setDescription(dto.getDescription());
		product.setActive(dto.isActive());

		InsuranceProduct updatedProduct = productRepository.save(product);

		log.info("Product updated successfully");

		return modelMapper.map(updatedProduct, ProductResponseDto.class);
	}

	@Override
	public ProductResponseDto deactivateProduct(Long id) {

		log.info("Deactivating product id: {}", id);

		InsuranceProduct product = findProductById(id);

		product.setActive(false);

		InsuranceProduct updatedProduct = productRepository.save(product);

		log.info("Product deactivated successfully");

		return modelMapper.map(updatedProduct, ProductResponseDto.class);
	}

	@Override
	public ProductResponseDto getProductById(Long id) {

		log.info("Fetching product by id: {}", id);

		InsuranceProduct product = findProductById(id);

		return modelMapper.map(product, ProductResponseDto.class);
	}

	@Override
	public Page<ProductResponseDto> getAllProducts(Pageable pageable) {

		log.info("Fetching all products");

		return productRepository.findAll(pageable).map(product -> modelMapper.map(product, ProductResponseDto.class));
	}

	private InsuranceProduct findProductById(Long id) {

		return productRepository.findById(id).orElseThrow(() -> {

			log.error("Product not found with id: {}", id);

			return new ResourceNotFoundException("Product not found with id: " + id);
		});
	}
}
