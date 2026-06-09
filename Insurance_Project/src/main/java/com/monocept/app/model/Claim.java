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
import com.monocept.app.enums.ClaimStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "claims",
    indexes = {
        @Index(name = "idx_claim_number",  columnList = "claim_number"),
        @Index(name = "idx_claim_status",  columnList = "claim_status"),
        @Index(name = "idx_claim_policy",  columnList = "policy_id"),
        @Index(name = "idx_incident_date", columnList = "incident_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"policy", "statusHistory", "documents"})
@EqualsAndHashCode(exclude = {"policy", "statusHistory", "documents"})
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    private Long id;

    @NotBlank(message = "Claim number is required")
    @Pattern(
        regexp = "^CLM-[0-9]{8}-[A-Z0-9]{6}$",
        message = "Claim number must follow format: CLM-YYYYMMDD-XXXXXX"
    )
    @Column(name = "claim_number", unique = true, nullable = false, length = 20)
    private String claimNumber;

    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Claim amount must be at least 0.01")
    @DecimalMax(value = "99999999.99", message = "Claim amount exceeds maximum allowed limit")
    @Digits(integer = 8, fraction = 2, message = "Claim amount must have at most 8 integer digits and 2 decimal places")
    @Column(name = "claim_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal claimAmount;

    @NotBlank(message = "Claim reason is required")
    @Size(min = 10, max = 1000, message = "Claim reason must be between 10 and 1000 characters")
    @Column(name = "claim_reason", nullable = false, length = 1000)
    private String claimReason;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be a future date")
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @NotNull(message = "Claim status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_status", nullable = false, length = 20)
    @Builder.Default
    private ClaimStatus claimStatus = ClaimStatus.SUBMITTED;

    @Size(max = 2000, message = "Agent remarks must not exceed 2000 characters")
    @Column(name = "agent_remarks", length = 2000)
    private String agentRemarks;

    @Size(max = 2000, message = "Admin remarks must not exceed 2000 characters")
    @Column(name = "admin_remarks", length = 2000)
    private String adminRemarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull(message = "Policy is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "policy_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_claim_policy")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "claims"})
    private Policy policy;

    @OneToMany(
        mappedBy = "claim",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<ClaimStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(
        mappedBy = "claim",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Builder.Default
    private List<ClaimDocument> documents = new ArrayList<>();
}