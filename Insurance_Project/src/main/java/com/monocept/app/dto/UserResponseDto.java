package com.monocept.app.dto;

import java.time.LocalDate;

import com.monocept.app.enums.AgentSpecialization;
import com.monocept.app.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
  
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private AgentSpecialization specialization;
    private boolean active;
    private LocalDate createDate;
}

