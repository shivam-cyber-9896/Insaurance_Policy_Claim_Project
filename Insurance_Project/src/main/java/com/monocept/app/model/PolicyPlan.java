package com.monocept.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monocept.app.enums.PremiumType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "policy_plans",
    indexes = {
        @Index(name = "idx_plan_name",       columnList = "plan_name"),
        @Index(name = "idx_plan_product",    columnList = "product_id"),
        @Index(name = "idx_plan_type",       columnList = "premium_type"),
        @Index(name = "idx_plan_active",     columnList = "active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"insuranceProduct", "policies"})
@EqualsAndHashCode(exclude = {"insuranceProduct", "policies"})
public class PolicyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    @NotNull(message = "Insurance product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_plan_product")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "policyPlans"})
    private InsuranceProduct insuranceProduct;

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 100, message = "Plan name must be between 3 and 100 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9 &()\\-]+$",
        message = "Plan name can only contain letters, digits, spaces, &, (), or hyphens"
    )
    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Coverage amount must be greater than zero")
    @DecimalMax(value = "99999999.99", message = "Coverage amount exceeds maximum allowed limit")
    @Digits(integer = 8, fraction = 2, message = "Coverage amount must have at most 8 integer digits and 2 decimal places")
    @Column(name = "coverage_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal coverageAmount;

    @NotNull(message = "Premium amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Premium amount must be greater than zero")
    @DecimalMax(value = "999999.99", message = "Premium amount exceeds maximum allowed limit")
    @Digits(integer = 6, fraction = 2, message = "Premium amount must have at most 6 integer digits and 2 decimal places")
    @Column(name = "premium_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal premiumAmount;

    @NotNull(message = "Premium type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "premium_type", nullable = false, length = 20)
    private PremiumType premiumType;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 40, message = "Duration cannot exceed 40 years")
    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;

    @NotBlank(message = "Terms and conditions are required")
    @Size(min = 20, max = 5000, message = "Terms and conditions must be between 20 and 5000 characters")
    @Column(name = "terms_and_conditions", nullable = false, length = 5000)
    private String termsAndConditions;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "policyPlan",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<Policy> policies = new ArrayList<>();
}