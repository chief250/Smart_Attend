package com.smartattend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrTokenDto {
    private Long id;
    private Long sessionId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private String status;
    private LocalDateTime createdAt;
}
