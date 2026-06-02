package com.example.app.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.app.enums.PremiumType;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Policy Plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Plan Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Product Id", nullable = false)
    private InsuranceProduct insuranceProduct;

    @NotBlank(message = "Plan name is required")
    @Column(name = "Plan Name")
    private String planName;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.01",
            message = "Coverage amount must be greater than zero")
    @Column(name = "Coverage Amount")
    private Double coverageAmount;

    @NotNull(message = "Premium amount is required")
    @DecimalMin(value = "0.01",
            message = "Premium amount must be greater than zero")
    @Column(name = "Premium Amount")
    private Double premiumAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "Premium Type", nullable=false)
    private PremiumType premiumType;

    @Column(name = "Duration")
    @Min(value = 1, message = "Duration cannot be less than 1 year")
    private Integer duration;

    @Column(name = "Terms and Conditions", length = 5000)
    @NotBlank
    private String termsAndConditions;

    @Column(name = "Active Status")
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "Created Date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "Updated Date")
    private LocalDateTime updatedDate;
    
    @OneToMany(mappedBy = "policyPlan")
    private List<Policy> policies;
}