package com.monocept.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.monocept.app.model.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmail(String email);
}
