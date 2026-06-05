package com.monocept.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monocept.app.dto.PlanRequestDto;
import com.monocept.app.dto.PlanResponseDto;
import com.monocept.app.service.PolicyPlanService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PolicyPlanService planService;

    @PostMapping
    public ResponseEntity<PlanResponseDto>
    createPlan(
            @Valid @RequestBody PlanRequestDto dto) {

        return ResponseEntity.ok(
                planService.createPlan(dto));
    }

    @GetMapping
    public ResponseEntity<Page<PlanResponseDto>>
    getAllPlans(Pageable pageable) {

        return ResponseEntity.ok(
                planService.getAllPlans(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponseDto>
    getPlanById(@PathVariable Long id) {

        return ResponseEntity.ok(
                planService.getPlanById(id));
    }
}