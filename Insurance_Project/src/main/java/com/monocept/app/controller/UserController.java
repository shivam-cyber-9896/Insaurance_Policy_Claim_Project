package com.monocept.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monocept.app.dto.UserResponseDto;
import com.monocept.app.dto.UserStatusRequestDto;
import com.monocept.app.service.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>>
    getAllUsers(Pageable pageable) {

        return ResponseEntity.ok(
                userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto>
    getUserById(@PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<UserResponseDto>
    activateUser(
            @PathVariable Long id,
            @RequestBody UserStatusRequestDto dto) {

        return ResponseEntity.ok(
                userService.activateUser(id, dto));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseDto>
    deactivateUser(
            @PathVariable Long id,
            @RequestBody UserStatusRequestDto dto) {

        return ResponseEntity.ok(
                userService.deactivateUser(id, dto));
    }
}