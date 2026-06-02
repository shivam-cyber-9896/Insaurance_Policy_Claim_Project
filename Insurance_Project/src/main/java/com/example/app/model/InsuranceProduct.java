package com.example.app.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.app.enums.ProductType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Insurance Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="Product Id")
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(name="Product Name", unique = true)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name="Product Type", nullable=false)
    private ProductType productType;

    @Column(name="Description")
    @NotBlank
    private String description;

    @Column(name="Active Status")
    private boolean active = true;

    @CreationTimestamp
    @Column(name="Created Date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name="Updated Date")
    private LocalDateTime updatedDate;
    
    @OneToMany(mappedBy = "insuranceProduct")
    private List<PolicyPlan> policyPlans;
}