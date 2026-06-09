package com.monocept.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monocept.app.enums.PolicyStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "policies",
    indexes = {
        @Index(name = "idx_policy_number",   columnList = "policy_number"),
        @Index(name = "idx_policy_status",   columnList = "policy_status"),
        @Index(name = "idx_policy_customer", columnList = "customer_id"),
        @Index(name = "idx_policy_plan",     columnList = "plan_id"),
        @Index(name = "idx_policy_end_date", columnList = "end_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"customer", "policyPlan", "claims", "premiumPayments"})
@EqualsAndHashCode(exclude = {"customer", "policyPlan", "claims", "premiumPayments"})
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @NotBlank(message = "Policy number is required")
    @Pattern(
        regexp = "^POL-[0-9]{8}-[A-Z0-9]{6}$",
        message = "Policy number must follow format: POL-YYYYMMDD-XXXXXX"
    )
    @Column(name = "policy_number", unique = true, nullable = false, length = 20)
    private String policyNumber;

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Policy status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_status", nullable = false, length = 30)
    @Builder.Default
    private PolicyStatus policyStatus = PolicyStatus.ACTIVE;

    @NotNull(message = "Total premium paid cannot be null")
    @DecimalMin(value = "0.00", inclusive = true, message = "Total premium paid cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Total premium paid must have at most 10 integer digits and 2 decimal places")
    @Column(name = "total_premium_paid", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPremiumPaid = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_policy_customer")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "policies"})
    private Customer customer;

    @NotNull(message = "Policy plan is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "plan_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_policy_plan")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "policies"})
    private PolicyPlan policyPlan;

    @OneToMany(
        mappedBy = "policy",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<Claim> claims = new ArrayList<>();

    @OneToMany(
        mappedBy = "policy",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<PremiumPayment> premiumPayments = new ArrayList<>();
}