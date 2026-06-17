package com.monocept.app.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "otp_verifications",
    indexes = {
        @Index(name = "idx_otp_email", columnList = "email"),
        @Index(name = "idx_otp_phone", columnList = "phone_number")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
    @Column(name = "phone_number", length = 10)
    private String phoneNumber;

    @NotBlank(message = "OTP is required")
    @Column(name = "otp", nullable = false, length = 6)
    private String otp;

    @NotNull(message = "Expiry time is required")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}