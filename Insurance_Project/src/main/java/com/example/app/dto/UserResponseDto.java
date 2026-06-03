package com.example.app.dto;

import com.example.app.enums.Role;
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
    private long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private boolean active;
    private LocalDate createDate;
}
