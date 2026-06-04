package com.monocept.app.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.monocept.app.enums.ClaimStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Claim Status History")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "History Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Claim Id", nullable = false)
    private Claim claim;

    @Enumerated(EnumType.STRING)
    @Column(name = "Previous Status")
    private ClaimStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "New Status")
    private ClaimStatus newStatus;

    @Column(name = "Remarks", length = 2000)
    private String remarks;


    @CreationTimestamp
    @Column(name = "Updated Date")
    private LocalDateTime updatedDate;
    
    @ManyToOne
    @JoinColumn(name = "Updated By", nullable = false)
    private User updatedBy;
}