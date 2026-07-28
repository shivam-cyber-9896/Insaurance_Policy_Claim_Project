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

    @Version
   @Column(name = "version")
    private Long version;
 
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

    @NotNull(message = "Remaining coverage cannot be null")
    @DecimalMin(value = "0.00", inclusive = true, message = "Remaining coverage cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Remaining coverage must have at most 8 integer digits and 2 decimal places")
    @Column(name = "remaining_coverage", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingCoverage;

    // Customer's chosen sum insured within [plan.min, plan.max]
    @Column(name = "selected_coverage_amount", precision = 15, scale = 2)
    private BigDecimal selectedCoverageAmount;

    // Locked billing frequency chosen at purchase time
    @Enumerated(EnumType.STRING)
    @Column(name = "premium_type", length = 20)
    private com.monocept.app.enums.PremiumType premiumType;

    // Locked calculated installment premium at purchase time
    @Column(name = "premium_amount", precision = 15, scale = 2)
    private BigDecimal premiumAmount;

    // Policyholder detail fields
    @Column(name = "holder_name", length = 100)
    private String holderName;

    @Column(name = "holder_address", length = 500)
    private String holderAddress;

    @Column(name = "holder_phone", length = 10)
    private String holderPhone;

    @Column(name = "holder_aadhaar", length = 255)
    private String holderAadhaar;

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "customer_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_policy_customer")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "policies"})
    private Customer customer;

    @NotNull(message = "Policy plan is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "plan_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_policy_plan")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "policies"})
    private PolicyPlan policyPlan;

    /**
     * The agent assigned to this policy.
     * Must have specialization matching the policy plan's product type, or SUPER.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "agent_id",
        nullable = true,
        foreignKey = @ForeignKey(name = "fk_policy_agent")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "customer"})
    private com.monocept.app.model.User agent;

    @OneToMany(
        mappedBy = "policy",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JsonIgnore
    @Builder.Default
    private List<Claim> claims = new ArrayList<>();

    @OneToMany(
        mappedBy = "policy",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JsonIgnore
    @Builder.Default
    private List<PremiumPayment> premiumPayments = new ArrayList<>();
}