package com.monocept.app.service;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.monocept.app.dto.PolicyIssueRequestDto;
import com.monocept.app.dto.PolicyPurchaseRequestDto;
import com.monocept.app.dto.PolicyResponseDto;

public interface PolicyService {

	PolicyResponseDto purchasePolicy(PolicyPurchaseRequestDto dto);

	PolicyResponseDto issuePolicy(PolicyIssueRequestDto dto);

	PolicyResponseDto getPolicyById(Long id);

	PolicyResponseDto cancelPolicy(Long id);

	Page<PolicyResponseDto> getAllPolicies(Pageable pageable);

	Page<PolicyResponseDto> getMyPolicies(Pageable pageable);

	
}