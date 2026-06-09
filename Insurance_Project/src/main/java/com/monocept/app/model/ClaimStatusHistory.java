package com.monocept.app.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monocept.app.enums.ClaimStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "claim_status_history",
    indexes = {
        @Index(name = "idx_csh_claim_id",    columnList = "claim_id"),
        @Index(name = "idx_csh_new_status",  columnList = "new_status"),
        @Index(name = "idx_csh_updated_by",  columnList = "updated_by")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"claim", "updatedBy"})
@EqualsAndHashCode(exclude = {"claim", "updatedBy"})
public class ClaimStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @NotNull(message = "Claim is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "claim_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_csh_claim")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "statusHistory"})
    private Claim claim;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private ClaimStatus previousStatus;

    @NotNull(message = "New status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private ClaimStatus newStatus;

    @Size(max = 2000, message = "Remarks must not exceed 2000 characters")
    @Column(name = "remarks", length = 2000)
    private String remarks;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false, nullable = false)
    private LocalDateTime changedAt;

    @NotNull(message = "Updated by user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "updated_by",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_csh_updated_by")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User updatedBy;
}