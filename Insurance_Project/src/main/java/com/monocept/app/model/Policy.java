package com.monocept.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.monocept.app.enums.PolicyStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Policy Id")
	private Long id;

	@NotBlank(message = "Policy number is required")
	@Column(name = "Policy Number", unique = true)
	private String policyNumber;

	@Column(name = "Start Date")
	private LocalDate startDate;

	@Column(name = "End Date")
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "Policy Status")
	private PolicyStatus policyStatus;

	@Column(name = "Total Premium Paid")
	private BigDecimal totalPremiumPaid = BigDecimal.ZERO;

	@CreationTimestamp
	@Column(name = "Created Date")
	private LocalDateTime createdDate;

	@UpdateTimestamp
	@Column(name = "Updated Date")
	private LocalDateTime updatedDate;

	@ManyToOne
	@JoinColumn(name = "Customer Id", nullable = false)
	private Customer customer;

	@ManyToOne
	@JoinColumn(name = "Plan Id", nullable = false)
	private PolicyPlan policyPlan;
	
	@OneToMany(mappedBy = "policy")
	private List<Claim> claims;
	
	@OneToMany(mappedBy = "policy")
	private List<PremiumPayment> premiumPayments;
}