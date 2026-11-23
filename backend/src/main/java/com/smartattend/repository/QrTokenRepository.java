package com.smartattend.repository;

import com.smartattend.model.entity.QrToken;
import com.smartattend.model.enums.QrTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrTokenRepository extends JpaRepository<QrToken, Long> {
    Optional<QrToken> findByTokenHash(String tokenHash);
    Optional<QrToken> findBySessionIdAndStatus(Long sessionId, QrTokenStatus status);
}
