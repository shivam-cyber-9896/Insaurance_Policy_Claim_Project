package com.monocept.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.monocept.app.enums.PaymentMode;
import com.monocept.app.enums.PaymentStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Premium Payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumPayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Payment Id")
	private Long id;

	@NotNull(message = "Payment amount is required")
	@DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
	@Column(name = "Amount")
	private BigDecimal amount;

	@Column(name = "Payment Date")
	private LocalDateTime paymentDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "Payment Mode")
	private PaymentMode paymentMode;

	@Column(name = "Transaction Reference", unique = true)
	private String transactionReference;

	@Enumerated(EnumType.STRING)
	@Column(name = "Payment Status")
	private PaymentStatus paymentStatus;

	@CreationTimestamp
	@Column(name = "Created Date")
	private LocalDateTime createdDate;

	@ManyToOne
	@JoinColumn(name = "Policy Id", nullable = false)
	private Policy policy;
}