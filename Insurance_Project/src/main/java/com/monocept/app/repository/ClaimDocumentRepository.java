package com.monocept.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.model.Claim;
import com.monocept.app.model.ClaimDocument;

public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {

	List<ClaimDocument> findByClaim(Claim claim);
	 List<ClaimDocument> findByClaimId(Long claimId);
}
