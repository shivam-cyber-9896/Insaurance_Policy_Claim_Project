package com.monocept.app.service.implementation;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.UserRequestDto;
import com.monocept.app.dto.UserResponseDto;
import com.monocept.app.dto.UserStatusRequestDto;
import com.monocept.app.exception.CustomExceptions.DuplicateResourceException;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.User;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

	@Override
	public Page<UserResponseDto> getAllUsers(Pageable pageable) {

		log.info("Fetching all users");

		return userRepository.findAll(pageable).map(this::convertToDto);
	}

	@Override
	public UserResponseDto getUserById(Long id) {

		log.info("Fetching user with id: {}", id);

		User user = findUserById(id);

		return convertToDto(user);
	}

	@Override
	public UserResponseDto activateUser(Long id, UserStatusRequestDto dto) {

		log.info("Activating user with id: {}", id);

		User user = findUserById(id);

		user.setActive(true);

		User updatedUser = userRepository.save(user);

		log.info("User activated successfully");

		return convertToDto(updatedUser);
	}

	@Override
	public UserResponseDto deactivateUser(Long id, UserStatusRequestDto dto) {

		log.info("Deactivating user with id: {}", id);

		User user = findUserById(id);

		user.setActive(false);

		User updatedUser = userRepository.save(user);

		log.info("User deactivated successfully");

		return convertToDto(updatedUser);
	}


	private User findUserById(Long id) {

		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	private UserResponseDto convertToDto(User user) {

		return modelMapper.map(user, UserResponseDto.class);
	}

	@Override
	public UserResponseDto createAgent(UserRequestDto dto) {
		log.info("Creating agent");

		if (userRepository.existsByMail(dto.getEmail())) {

			throw new DuplicateResourceException("Email already exists");
		}

		User user = modelMapper.map(dto, User.class);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(com.monocept.app.enums.Role.AGENT);
		user.setActive(true);

		User savedUser = userRepository.save(user);

		log.info("Agent created successfully");

		return convertToDto(savedUser);
	}

}