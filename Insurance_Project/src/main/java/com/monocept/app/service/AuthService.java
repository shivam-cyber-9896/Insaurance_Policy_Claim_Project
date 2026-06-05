package com.monocept.app.service;

import com.monocept.app.dto.LoginRequestDto;
import com.monocept.app.dto.LoginResponseDto;
import com.monocept.app.dto.UserRequestDto;
import com.monocept.app.dto.UserResponseDto;

public interface AuthService {
	UserResponseDto register(UserRequestDto dto);

	LoginResponseDto login(LoginRequestDto dto);
}
