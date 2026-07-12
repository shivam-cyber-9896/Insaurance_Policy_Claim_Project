package com.monocept.app.service;

import java.util.List;

import com.monocept.app.dto.UserRequestDto;
import com.monocept.app.dto.UserResponseDto;
import com.monocept.app.dto.UserStatusRequestDto;
import com.monocept.app.enums.AgentSpecialization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto getUserById(Long id);

    UserResponseDto activateUser(Long id, UserStatusRequestDto dto);

    UserResponseDto deactivateUser(Long id, UserStatusRequestDto dto);

    UserResponseDto createAgent(UserRequestDto dto);

    /**
     * Returns agents filtered by specialization.
     * Also returns all SUPER_AGENTs (who can handle all types).
     * Used by frontend when customer/admin selects an agent during policy creation.
     */
    List<UserResponseDto> getAgentsBySpecialization(AgentSpecialization specialization);
}
