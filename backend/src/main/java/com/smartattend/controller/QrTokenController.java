package com.smartattend.controller;

import com.smartattend.model.dto.ApiResponse;
import com.smartattend.model.dto.AttendanceSessionDto;
import com.smartattend.model.dto.QrTokenDto;
import com.smartattend.model.entity.QrToken;
import com.smartattend.service.AttendanceSessionService;
import com.smartattend.service.QrTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QrTokenController {

    private final QrTokenService qrTokenService;
    private final AttendanceSessionService sessionService;

    @PostMapping("/{sessionId}/qr")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<QrTokenDto>> generateQrToken(@PathVariable Long sessionId) {
        QrTokenDto tokenDto = qrTokenService.generateToken(sessionId);
        return ResponseEntity.ok(ApiResponse.success("QR token generated successfully", tokenDto));
    }

    @GetMapping("/{sessionId}/qr")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<QrTokenDto>> getActiveQrToken(@PathVariable Long sessionId) {
        QrTokenDto tokenDto = qrTokenService.getActiveTokenForSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(tokenDto));
    }

    @GetMapping("/qr/validate")
    public ResponseEntity<ApiResponse<AttendanceSessionDto>> validateQrToken(@RequestParam String token) {
        QrToken qrToken = qrTokenService.validateToken(token);
        AttendanceSessionDto sessionDto = sessionService.toDto(qrToken.getSession());
        return ResponseEntity.ok(ApiResponse.success("QR Token is valid", sessionDto));
    }
}
