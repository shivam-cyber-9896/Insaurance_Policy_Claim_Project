package com.monocept.app.service.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.UserRequestDto;
import com.monocept.app.dto.UserResponseDto;
import com.monocept.app.dto.UserStatusRequestDto;
import com.monocept.app.enums.AgentSpecialization;
import com.monocept.app.enums.Role;
import com.monocept.app.exception.CustomExceptions.DuplicateResourceException;
import com.monocept.app.exception.InvalidOperationException;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.User;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.service.EmailService;
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
	private final EmailService emailService;
	private final EmailTemplateServiceImpl emailTemplateService;

	@Override
	public Page<UserResponseDto> getAllUsers(Pageable pageable) {
		log.info("Fetching all users");
		return userRepository.findAll(pageable).map(this::convertToDto);
	}

	@Override
	public UserResponseDto getUserById(Long id) {
		log.info("Fetching user with id: {}", id);
		return convertToDto(findUserById(id));
	}

	@Override
	@org.springframework.cache.annotation.CacheEvict(value = "users", allEntries = true)
	public UserResponseDto activateUser(Long id, UserStatusRequestDto dto) {
		log.info("Activating user with id: {}", id);
		User user = findUserById(id);
		user.setActive(true);
		User updatedUser = userRepository.save(user);
		emailService.sendEmail(updatedUser.getEmail(), "Account Activated",
				emailTemplateService.accountStatusTemplate(updatedUser.getFullName(), "activated"));
		log.info("User activated successfully");
		return convertToDto(updatedUser);
	}

	@Override
	@org.springframework.cache.annotation.CacheEvict(value = "users", allEntries = true)
	public UserResponseDto deactivateUser(Long id, UserStatusRequestDto dto) {
		log.info("Deactivating user with id: {}", id);
		User user = findUserById(id);
		user.setActive(false);
		User updatedUser = userRepository.save(user);
		log.info("User deactivated successfully");
		return convertToDto(updatedUser);
	}

	@Override
	public UserResponseDto createAgent(UserRequestDto dto) {
		log.info("Creating agent/super-agent");

		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new DuplicateResourceException("Email already exists");
		}

		// Only AGENT or SUPER_AGENT roles are allowed through this endpoint
		if (dto.getRole() != Role.AGENT && dto.getRole() != Role.SUPER_AGENT) {
			throw new InvalidOperationException("This endpoint is only for creating AGENT or SUPER_AGENT users");
		}

		// Standard AGENT must have a specialization (HEALTH/MOTOR/LIFE/TRAVEL — not SUPER)
		if (dto.getRole() == Role.AGENT) {
			if (dto.getSpecialization() == null) {
				throw new InvalidOperationException("Specialization is required for AGENT role");
			}
			if (dto.getSpecialization() == AgentSpecialization.SUPER) {
				throw new InvalidOperationException(
						"An AGENT cannot have SUPER specialization. Use SUPER_AGENT role instead.");
			}
		}

		// SUPER_AGENT automatically gets SUPER specialization
		if (dto.getRole() == Role.SUPER_AGENT) {
			dto.setSpecialization(AgentSpecialization.SUPER);
		}

		User user = modelMapper.map(dto, User.class);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(dto.getRole());
		user.setSpecialization(dto.getSpecialization());
		user.setActive(true);

		User savedUser = userRepository.save(user);
		emailService.sendEmail(savedUser.getEmail(), "Agent Account Created",
				emailTemplateService.agentRegisteredTemplate(savedUser.getFullName(), savedUser.getEmail(),
						dto.getPassword() // raw password before encoding
				));
		log.info("Agent/super-agent created successfully: role={}, specialization={}", savedUser.getRole(),
				savedUser.getSpecialization());

		return convertToDto(savedUser);
	}

	@Override
	public List<UserResponseDto> getAgentsBySpecialization(AgentSpecialization specialization) {
		log.info("Fetching agents for specialization: {}", specialization);

		List<User> result = new ArrayList<>();

		// Get all agents with exact matching specialization
		result.addAll(userRepository.findByRoleAndSpecialization(Role.AGENT, specialization));

		// Always include all SUPER_AGENTs — they can handle any type
		result.addAll(userRepository.findByRoleAndSpecialization(Role.SUPER_AGENT, AgentSpecialization.SUPER));

		return result.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	private User findUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	private UserResponseDto convertToDto(User user) {
		return modelMapper.map(user, UserResponseDto.class);
	}
}