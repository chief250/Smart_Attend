package com.smartattend.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionDto {
    private Long id;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private Long lecturerId;
    private String lecturerName;
    private LocalDate sessionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int lateThresholdMinutes;
    private String status;
    private LocalDateTime createdAt;
    private boolean attended;
    private String attendanceStatus;
}
