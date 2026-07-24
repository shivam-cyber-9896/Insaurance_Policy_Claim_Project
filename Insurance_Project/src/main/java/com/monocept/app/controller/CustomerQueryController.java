package com.monocept.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.monocept.app.dto.CustomerQueryReplyDto;
import com.monocept.app.dto.CustomerQueryRequestDto;
import com.monocept.app.dto.CustomerQueryResponseDto;
import com.monocept.app.service.CustomerQueryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/queries")
@RequiredArgsConstructor
public class CustomerQueryController {

    private final CustomerQueryService queryService;

    @PostMapping
    public ResponseEntity<CustomerQueryResponseDto> createQuery(@Valid @RequestBody CustomerQueryRequestDto dto) {
        return ResponseEntity.ok(queryService.createQuery(dto));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<CustomerQueryResponseDto>> getMyQueries(Pageable pageable) {
        return ResponseEntity.ok(queryService.getMyQueries(pageable));
    }

    @GetMapping
    public ResponseEntity<Page<CustomerQueryResponseDto>> getAllQueries(Pageable pageable) {
        return ResponseEntity.ok(queryService.getAllQueries(pageable));
    }

    @PutMapping("/{id}/reply")
    public ResponseEntity<CustomerQueryResponseDto> replyToQuery(@PathVariable Long id, @Valid @RequestBody CustomerQueryReplyDto dto) {
        return ResponseEntity.ok(queryService.replyToQuery(id, dto));
    }
}
