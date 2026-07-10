package com.monocept.app.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VersionMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        log.info("Starting data migration to backfill version for optimistic locking...");
        
        int policiesUpdated = jdbcTemplate.update("UPDATE policies SET version = 0 WHERE version IS NULL");
        log.info("Updated {} policies with version = 0", policiesUpdated);
        
        int claimsUpdated = jdbcTemplate.update("UPDATE claims SET version = 0 WHERE version IS NULL");
        log.info("Updated {} claims with version = 0", claimsUpdated);
        
        int paymentsUpdated = jdbcTemplate.update("UPDATE premium_payments SET version = 0 WHERE version IS NULL");
        log.info("Updated {} premium payments with version = 0", paymentsUpdated);
        
        log.info("Version migration completed.");
    }
}
