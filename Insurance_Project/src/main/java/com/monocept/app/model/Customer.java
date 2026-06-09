package com.monocept.app.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "customers", indexes = { @Index(name = "idx_customer_city", columnList = "city"),
		@Index(name = "idx_customer_state", columnList = "state"),
		@Index(name = "idx_customer_pincode", columnList = "pin_code") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // replaces the broken hand-written builder()
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "customer_id")
	private Long id;

	@NotNull(message = "User is required")
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true, nullable = false, foreignKey = @ForeignKey(name = "fk_customer_user"))
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "customer" })
	private User user;

	@NotNull(message = "Date of birth is required")
	@Past(message = "Date of birth must be in the past")
	@Column(name = "date_of_birth", nullable = false)
	private LocalDate dateOfBirth;

	@NotBlank(message = "Address is required")
	@Size(max = 255, message = "Address must not exceed 255 characters")
	@Column(name = "address", nullable = false, length = 255)
	private String address;

	@NotBlank(message = "City is required")
	@Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
	@Pattern(regexp = "^[a-zA-Z\\s\\-\\.]+$", message = "City can only contain letters, spaces, hyphens, or dots")
	@Column(name = "city", nullable = false, length = 100)
	private String city;

	@NotBlank(message = "State is required")
	@Size(min = 2, max = 100, message = "State must be between 2 and 100 characters")
	@Pattern(regexp = "^[a-zA-Z\\s\\-\\.]+$", message = "State can only contain letters, spaces, hyphens, or dots")
	@Column(name = "state", nullable = false, length = 100)
	private String state;

	@NotBlank(message = "PIN code is required")
	@Pattern(regexp = "^[1-9][0-9]{5}$", message = "PIN code must be a valid 6-digit Indian postal code not starting with 0")
	@Column(name = "pin_code", nullable = false, length = 6)
	private String pinCode;

	@NotBlank(message = "Nominee name is required")
	@Size(min = 2, max = 100, message = "Nominee name must be between 2 and 100 characters")
	@Pattern(regexp = "^[a-zA-Z .'\\-]+$", message = "Nominee name can only contain letters, spaces, dots, apostrophes, or hyphens")
	@Column(name = "nominee_name", nullable = false, length = 100)
	private String nomineeName;

	@NotBlank(message = "Nominee relation is required")
	@Size(max = 50, message = "Nominee relation must not exceed 50 characters")
	@Pattern(regexp = "^(Spouse|Parent|Sibling|Child|Friend|Relative|Other)$", message = "Nominee relation must be one of: Spouse, Parent, Sibling, Child, Friend, Relative, Other")
	@Column(name = "nominee_relation", nullable = false, length = 50)
	private String nomineeRelation;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JsonIgnore
	@Builder.Default
	private List<Policy> policies = new ArrayList<>();
}