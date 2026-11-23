package com.smartattend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QrScanRequest {

    @NotBlank(message = "QR token is required")
    private String qrToken;

    @NotNull(message = "Face verification result is required")
    private Boolean faceVerified;

    @NotNull(message = "Student ID is required")
    private Long studentId;
}
