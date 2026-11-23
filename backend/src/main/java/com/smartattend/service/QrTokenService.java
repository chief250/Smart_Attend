package com.smartattend.service;

import com.smartattend.exception.QrTokenExpiredException;
import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.model.dto.QrTokenDto;
import com.smartattend.model.entity.AttendanceSession;
import com.smartattend.model.entity.QrToken;
import com.smartattend.model.enums.QrTokenStatus;
import com.smartattend.model.enums.SessionStatus;
import com.smartattend.repository.AttendanceSessionRepository;
import com.smartattend.repository.QrTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrTokenService {

    private static final int TOKEN_VALIDITY_MINUTES = 5;

    private final QrTokenRepository qrTokenRepository;
    private final AttendanceSessionRepository sessionRepository;

    @Transactional
    public QrTokenDto generateToken(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));

        if (session.getStatus() != SessionStatus.OPEN) {
            throw new IllegalArgumentException("Session is not open. Cannot generate QR token.");
        }

        // Expire any existing active tokens for this session
        qrTokenRepository.findBySessionIdAndStatus(sessionId, QrTokenStatus.ACTIVE).ifPresent(existing -> {
            existing.setStatus(QrTokenStatus.EXPIRED);
            qrTokenRepository.save(existing);
        });

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = sha256Hash(rawToken);

        QrToken qrToken = QrToken.builder()
                .session(session)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES))
                .status(QrTokenStatus.ACTIVE)
                .build();

        qrTokenRepository.save(qrToken);

        return QrTokenDto.builder()
                .id(qrToken.getId())
                .sessionId(sessionId)
                .tokenHash(rawToken) // Return the unhashed raw token to the client (to embed in QR)
                .expiresAt(qrToken.getExpiresAt())
                .status(qrToken.getStatus().name())
                .createdAt(qrToken.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public QrToken validateToken(String rawToken) {
        String tokenHash = sha256Hash(rawToken);

        QrToken qrToken = qrTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException("QrToken", "token", "provided value"));

        if (qrToken.getStatus() == QrTokenStatus.USED) {
            throw new QrTokenExpiredException("QR token has already been used.");
        }
        if (qrToken.getStatus() == QrTokenStatus.EXPIRED || LocalDateTime.now().isAfter(qrToken.getExpiresAt())) {
            qrToken.setStatus(QrTokenStatus.EXPIRED);
            qrTokenRepository.save(qrToken);
            throw new QrTokenExpiredException("QR token has expired. Please scan a fresh QR code.");
        }

        return qrToken;
    }

    @Transactional
    public void markTokenUsed(QrToken qrToken) {
        qrToken.setStatus(QrTokenStatus.USED);
        qrTokenRepository.save(qrToken);
    }

    @Transactional(readOnly = true)
    public QrTokenDto getActiveTokenForSession(Long sessionId) {
        QrToken token = qrTokenRepository.findBySessionIdAndStatus(sessionId, QrTokenStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active QR token for session", "sessionId", sessionId));
        return QrTokenDto.builder()
                .id(token.getId())
                .sessionId(sessionId)
                .tokenHash(token.getTokenHash())
                .expiresAt(token.getExpiresAt())
                .status(token.getStatus().name())
                .createdAt(token.getCreatedAt())
                .build();
    }

    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
