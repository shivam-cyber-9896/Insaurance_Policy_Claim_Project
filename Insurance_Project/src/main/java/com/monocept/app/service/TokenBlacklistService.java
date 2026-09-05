package com.monocept.app.service;

import com.monocept.app.model.BlacklistedToken;
import com.monocept.app.repository.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public void blacklistToken(String token, LocalDateTime expiryDate) {
        if (!blacklistedTokenRepository.existsByToken(token)) {
            blacklistedTokenRepository.save(new BlacklistedToken(token, expiryDate));
            log.info("JWT successfully blacklisted until {}", expiryDate);
        }
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    // Auto-purges expired tokens every hour — keeping table small and lookups instant
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        blacklistedTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        log.info("Purged naturally expired tokens from blacklist table");
    }
}
