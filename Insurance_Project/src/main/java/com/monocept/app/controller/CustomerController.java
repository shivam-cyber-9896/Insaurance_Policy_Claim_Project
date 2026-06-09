package com.monocept.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monocept.app.dto.CustomerRequestDto;
import com.monocept.app.dto.CustomerResponseDto;
import com.monocept.app.service.CustomerService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

	private final CustomerService customerService;

	@PostMapping
	public ResponseEntity<CustomerResponseDto> createProfile(@Valid @RequestBody CustomerRequestDto dto) {

		return ResponseEntity.ok(customerService.createProfile(dto));
	}

	@GetMapping("/profile")
	public ResponseEntity<CustomerResponseDto> getMyProfile() {

		return ResponseEntity.ok(customerService.getMyProfile());
	}

	@GetMapping("/{id}")
	public ResponseEntity<CustomerResponseDto> getCustomer(@PathVariable Long id) {

		return ResponseEntity.ok(customerService.getCustomerById(id));
	}

	@GetMapping
	public ResponseEntity<Page<CustomerResponseDto>> getAllCustomers(Pageable pageable) {

		return ResponseEntity.ok(customerService.getAllCustomers(pageable));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CustomerResponseDto> updateCustomer(@PathVariable Long id,
			@RequestBody CustomerRequestDto dto) {

		return ResponseEntity.ok(customerService.updateProfile(id, dto));
	}
}