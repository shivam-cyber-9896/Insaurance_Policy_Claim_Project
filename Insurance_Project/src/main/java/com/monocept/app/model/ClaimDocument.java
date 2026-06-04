package com.monocept.app.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Claim Documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Document Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Claim Id", nullable = false)
    private Claim claim;

    @NotBlank(message = "Document name is required")
    @Column(name = "Document Name")
    private String documentName;

    @NotBlank(message = "Document type is required")
    @Column(name = "Document Type")
    private String documentType;

    @NotBlank(message = "Document reference is required")
    @Column(name = "Document Reference")
    private String documentReference;

    @CreationTimestamp
    @Column(name = "Uploaded Date")
    private LocalDateTime uploadedDate;
}