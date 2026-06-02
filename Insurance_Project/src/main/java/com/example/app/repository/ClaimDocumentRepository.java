package com.example.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.model.Claim;
import com.example.app.model.ClaimDocument;

public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {

	List<ClaimDocument> findByClaim(Claim claim);
}
