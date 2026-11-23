package com.smartattend.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateSessionRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    private int lateThresholdMinutes = 15;
}
