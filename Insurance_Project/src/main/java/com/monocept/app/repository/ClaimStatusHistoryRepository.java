package com.monocept.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.model.Claim;
import com.monocept.app.model.ClaimStatusHistory;

public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistory, Long> {

	List<ClaimStatusHistory> findByClaimOrderByChangedAtDesc(Claim claim);
}
