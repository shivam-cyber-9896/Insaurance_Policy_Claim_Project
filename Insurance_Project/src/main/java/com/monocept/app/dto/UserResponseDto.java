package com.monocept.app.dto;

import com.monocept.app.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
  
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private boolean active;
    private LocalDate createDate;
}
