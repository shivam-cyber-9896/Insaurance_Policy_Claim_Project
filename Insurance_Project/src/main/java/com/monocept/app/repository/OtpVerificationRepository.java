package com.monocept.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.enums.OtpPurpose;
import com.monocept.app.model.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, String> {

	Optional<OtpVerification> findByEmail(String email);

	Optional<OtpVerification> findByEmailAndPurpose(String email, OtpPurpose purpose);

	void deleteByEmailAndPurpose(String email, OtpPurpose purpose);
}