package com.monocept.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.monocept.app.enums.ClaimStatus;

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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Claim Id")
	private Long id;

	@Column(name = "Claim Number", unique = true)
	private String claimNumber;

	@NotNull(message = "Claim amount is required")
	@DecimalMin(value = "0.01", message = "Claim amount must be greater than zero")
	@Column(name = "Claim Amount")
	private BigDecimal claimAmount;

	@NotBlank(message = "Claim reason is required")
	@Column(name = "Claim Reason")
	private String claimReason;

	@NotNull(message = "Incident date is required")
	@PastOrPresent(message = "Incident date cannot be a future date")
	@Column(name = "Incident Date")
	private LocalDate incidentDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "Claim Status")
	private ClaimStatus claimStatus;

	@Column(name = "Agent Remarks", length = 2000)
	private String agentRemarks;

	@Column(name = "Admin Remarks", length = 2000)
	private String adminRemarks;

	@CreationTimestamp
	@Column(name = "Created Date")
	private LocalDateTime createdDate;

	@UpdateTimestamp
	@Column(name = "Updated Date")
	private LocalDateTime updatedDate;

	@ManyToOne
	@JoinColumn(name = "Policy Id", nullable = false)
	private Policy policy;
	
	@OneToMany(mappedBy = "claim")
	private List<ClaimStatusHistory> statusHistory;
	
	@OneToMany(mappedBy = "claim")
	private List<ClaimDocument> documents;
}