package com.monocept.app.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @Column(name="Customer Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "User Id", unique = true)
    private User user;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name="Date of Birth")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    @Column(name="Address")
    private String address;

    @NotBlank(message = "City is required")
    @Column(name="City")
    private String city;

    @NotBlank(message = "State is required")
    @Column(name="State")
    private String state;

    @NotBlank(message = "6 Digit PIN code is required")
    @Pattern(
        regexp = "^[1-9][0-9]{5}$",
        message = "Invalid PIN code"
    )
    @Column(name="PIN Code")
    private String pinCode;

    @NotBlank(message = "Nominee name is required")
    @Column(name="Nominee Name")
    private String nomineeName;

    @NotBlank(message = "Nominee relation is required")
    @Column(name="Nominee Relation")
    private String nomineeRelation;

    @CreationTimestamp
    @Column(name="Created Date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name="Updated Date")
    private LocalDateTime updatedDate;
    
    @OneToMany(mappedBy = "customer")
    private List<Policy> policies;
}
