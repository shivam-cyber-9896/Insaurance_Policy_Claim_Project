package com.monocept.app.model;

import java.time.LocalDateTime;

import com.monocept.app.enums.OtpPurpose;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "otp_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "mobile_otp")
    private String mobileOtp;

    @Column(name = "mobile_expires_at")
    private LocalDateTime mobileExpiresAt;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Builder.Default
    @Column(name = "mobile_verified", nullable = false)
    private boolean mobileVerified = false;
    @Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OtpPurpose purpose;
}
