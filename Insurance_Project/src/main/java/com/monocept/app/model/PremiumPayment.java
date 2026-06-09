package com.monocept.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.monocept.app.enums.PaymentMode;
import com.monocept.app.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "premium_payments",
    indexes = {
        @Index(name = "idx_payment_status", columnList = "payment_status"),
        @Index(name = "idx_policy_id", columnList = "policy_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Payment amount must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "payment_date", updatable = false, nullable = false)
    private LocalDateTime paymentDate;

    @NotNull(message = "Payment mode is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 30)
    private PaymentMode paymentMode;

    @Size(max = 100, message = "Transaction reference must not exceed 100 characters")
    @Pattern(
        regexp = "^[A-Za-z0-9\\-_]*$",
        message = "Transaction reference can only contain letters, digits, hyphens, or underscores"
    )
    @Column(name = "transaction_reference", unique = true, length = 100)
    private String transactionReference;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull(message = "Policy is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_payment_policy"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "premiumPayments"})
    private Policy policy;
}