package com.monocept.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.monocept.app.enums.ProductType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "insurance_products",
    indexes = {
        @Index(name = "idx_product_name", columnList = "product_name"),
        @Index(name = "idx_product_type", columnList = "product_type"),
        @Index(name = "idx_product_active", columnList = "active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "policyPlans")
@EqualsAndHashCode(exclude = "policyPlans")
public class InsuranceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9 &()\\-]+$",
        message = "Product name can only contain letters, digits, spaces, &, (), or hyphens"
    )
    @Column(name = "product_name", unique = true, nullable = false, length = 100)
    private String productName;

    @NotNull(message = "Product type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

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
        mappedBy = "insuranceProduct",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JsonIgnore
    @Builder.Default
    private List<PolicyPlan> policyPlans = new ArrayList<>();
}