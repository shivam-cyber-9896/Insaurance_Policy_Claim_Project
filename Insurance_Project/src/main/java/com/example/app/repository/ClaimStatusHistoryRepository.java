package com.example.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.model.Claim;
import com.example.app.model.ClaimStatusHistory;

public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistory, Long> {

	List<ClaimStatusHistory>
    findByClaimOrderByUpdatedDateDesc(
            Claim claim);
}
