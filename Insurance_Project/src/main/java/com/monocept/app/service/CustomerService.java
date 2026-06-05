package com.monocept.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.monocept.app.dto.CustomerRequestDto;
import com.monocept.app.dto.CustomerResponseDto;

public interface CustomerService {
	CustomerResponseDto createProfile(CustomerRequestDto dto);

    CustomerResponseDto updateProfile(Long id, CustomerRequestDto dto);

    CustomerResponseDto getCustomerById(Long id);

    CustomerResponseDto getMyProfile();

    Page<CustomerResponseDto> getAllCustomers(Pageable pageable);
}
