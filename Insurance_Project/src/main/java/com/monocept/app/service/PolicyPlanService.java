package com.monocept.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.monocept.app.dto.PlanRequestDto;
import com.monocept.app.dto.PlanResponseDto;

public interface PolicyPlanService {

    PlanResponseDto createPlan(PlanRequestDto dto);

    PlanResponseDto updatePlan(Long id, PlanRequestDto dto);

    PlanResponseDto deactivatePlan(Long id);

    PlanResponseDto getPlanById(Long id);

    List<PlanResponseDto> getPlansByProduct(Long productId);

    Page<PlanResponseDto> getAllPlans(Pageable pageable);
}
