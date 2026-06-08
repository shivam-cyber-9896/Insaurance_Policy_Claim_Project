package com.monocept.app.model;

import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.monocept.app.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="User Id")
	private long id;
	
	@Column(name="Full Name", nullable = false)
	@NotBlank(message = "Name cannot be empty")
	private String fullName;
	
	@Email(message = "Invalid Email Format")
	@Column(name="Email", unique = true)
	@NotBlank
	private String email;

	@Column(name="Password", nullable = false)
	@NotBlank
	@Size(min = 8, message = "Password must contain at least 8 characters")
	private String password;
	
	@Column(name="Mobile Number")
	@NotBlank
	@Pattern(
	    regexp = "^[6-9]\\d{9}$",
	    message = "Mobile number must be exactly 10 digits"
	)
	private String phoneNumber;
	
	@Column(name="Role", nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;
	
	@Column(name="Active Status", nullable = false)
	private boolean isActive=true;
	
	@Column(name="Created Date")
	@CreationTimestamp
	private LocalDate createDate;
	
	@Column(name="Updated Date")
	@UpdateTimestamp
	private LocalDate updateDate;
	
	@OneToOne(mappedBy = "user")
	private Customer customer;
	
}
