package com.monocept.app.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "claim_documents",
    indexes = {
        @Index(name = "idx_doc_claim_id",   columnList = "claim_id"),
        @Index(name = "idx_doc_type",       columnList = "document_type"),
        @Index(name = "idx_doc_reference",  columnList = "document_reference")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "claim")
@EqualsAndHashCode(exclude = "claim")
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long id;

    @NotNull(message = "Claim is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "claim_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_document_claim")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "documents"})
    private Claim claim;

    @NotBlank(message = "Document name is required")
    @Size(min = 3, max = 255, message = "Document name must be between 3 and 255 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9 .\\-_()]+$",
        message = "Document name can only contain letters, digits, spaces, dots, hyphens, underscores, or parentheses"
    )
    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @NotBlank(message = "Document type is required")
    @Pattern(
        regexp = "^(PDF|JPG|JPEG|PNG|TIFF|DOC|DOCX)$",
        message = "Document type must be one of: PDF, JPG, JPEG, PNG, TIFF, DOC, DOCX"
    )
    @Column(name = "document_type", nullable = false, length = 10)
    private String documentType;

    @NotBlank(message = "Document reference is required")
    @Size(max = 500, message = "Document reference must not exceed 500 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9/\\-_.]+$",
        message = "Document reference can only contain letters, digits, slashes, hyphens, underscores, or dots"
    )
    @Column(name = "document_reference", unique = true, nullable = false, length = 500)
    private String documentReference;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false, nullable = false)
    private LocalDateTime uploadedAt;
}