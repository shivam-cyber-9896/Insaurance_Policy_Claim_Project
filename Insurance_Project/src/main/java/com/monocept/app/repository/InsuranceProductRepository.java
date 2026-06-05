package com.monocept.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.model.InsuranceProduct;

public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, Long> {

	boolean existsByProductName(String productName);

	boolean existsByProductNameAndIdNot(String productName, Long id);

	List<InsuranceProduct> findByActiveTrue();
}