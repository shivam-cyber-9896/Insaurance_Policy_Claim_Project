package com.monocept.app.service;

import com.monocept.app.dto.UserResponseDto;
import com.monocept.app.dto.UserStatusRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
	Page<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto getUserById(Long id);

    UserResponseDto activateUser(Long id, UserStatusRequestDto dto);

    UserResponseDto deactivateUser(Long id, UserStatusRequestDto dto);

    UserResponseDto createAgent(UserResponseDto dto);
}
