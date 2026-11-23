package com.smartattend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull(message = "Attendance record ID is required")
    private Long attendanceId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String resolution;
}
