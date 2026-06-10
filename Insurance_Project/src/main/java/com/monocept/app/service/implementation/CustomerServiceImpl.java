package com.monocept.app.service.implementation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.CustomerRequestDto;
import com.monocept.app.dto.CustomerResponseDto;
import com.monocept.app.exception.CustomExceptions.DuplicateResourceException;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.Customer;
import com.monocept.app.model.User;
import com.monocept.app.repository.CustomerRepository;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.service.CustomerService;
import com.monocept.app.service.EmailService;
import com.monocept.app.service.EmailTempleteService;



@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	// add to injections
	private final EmailService emailService;
	private final EmailTempleteService emailTemplateService;
	@Override
	public CustomerResponseDto createProfile(
	        CustomerRequestDto dto) {

	    log.info("Creating customer profile");

	    String email = SecurityContextHolder
	            .getContext()
	            .getAuthentication()
	            .getName();

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException(
	                            "User not found"));

	    if (user.getRole() != com.monocept.app.enums.Role.CUSTOMER) {
	        throw new com.monocept.app.exception.InvalidOperationException("Only users with the CUSTOMER role can create a customer profile");
	    }

	    if(customerRepository.existsByUser(user)) {

	        throw new DuplicateResourceException(
	                "Customer profile already exists");
	    }

	    Customer customer =
	            modelMapper.map(dto, Customer.class);

	    customer.setUser(user);

	    Customer savedCustomer =
	            customerRepository.save(customer);
	    emailService.sendEmail(
	    	    savedCustomer.getUser().getEmail(),
	    	    "Customer Profile Created",
	    	    emailTemplateService.customerProfileCreatedTemplate(savedCustomer.getUser().getFullName())
	    	);
	    log.info("Customer profile created successfully");

	    return convertToDto(savedCustomer);
	}

	@Override
	public CustomerResponseDto updateProfile(Long id, CustomerRequestDto dto) {

		log.info("Updating customer profile");

		Customer customer = findCustomerById(id);

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) {
			if (!customer.getUser().getEmail().equals(email)) {
				throw new com.monocept.app.exception.InvalidOperationException("You are not authorized to update this profile");
			}
		}

		customer.setDateOfBirth(dto.getDateOfBirth());

		customer.setAddress(dto.getAddress());

		customer.setCity(dto.getCity());

		customer.setState(dto.getState());

		customer.setPinCode(dto.getPinCode());

		customer.setNomineeName(dto.getNomineeName());

		customer.setNomineeRelation(dto.getNomineeRelation());

		Customer updatedCustomer = customerRepository.save(customer);
		emailService.sendEmail(
			    updatedCustomer.getUser().getEmail(),
			    "Customer Profile Updated",
			    emailTemplateService.customerProfileUpdatedTemplate(updatedCustomer.getUser().getFullName())
			);

		return convertToDto(updatedCustomer);
	}

	@Override
	public CustomerResponseDto getCustomerById(Long id) {

		Customer customer = findCustomerById(id);

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getRole() == com.monocept.app.enums.Role.CUSTOMER) {
			if (!customer.getUser().getEmail().equals(email)) {
				throw new com.monocept.app.exception.InvalidOperationException("You are not authorized to view this profile");
			}
		}

		return convertToDto(customer);
	}

	@Override
	public CustomerResponseDto getMyProfile() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Customer customer = customerRepository.findByUser(user)
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
		return convertToDto(customer);
	}

	@Override
	public Page<CustomerResponseDto> getAllCustomers(Pageable pageable) {

		return customerRepository.findAll(pageable).map(this::convertToDto);
	}

	private Customer findCustomerById(Long id) {
		User user = userRepository.findById(id).orElse(null);
		if (user != null) {
			java.util.Optional<Customer> custOpt = customerRepository.findByUser(user);
			if (custOpt.isPresent()) {
				return custOpt.get();
			}
		}
		return customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
	}

	private CustomerResponseDto convertToDto(Customer customer) {

		CustomerResponseDto dto = modelMapper.map(customer, CustomerResponseDto.class);

	

		dto.setFullName(customer.getUser().getFullName());

		dto.setEmail(customer.getUser().getEmail());

		dto.setPhoneNumber(customer.getUser().getPhoneNumber());

		return dto;
	}
}