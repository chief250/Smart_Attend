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
public class AttendanceRecordDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Long sessionId;
    private String courseName;
    private String courseCode;
    private LocalDateTime checkInTime;
    private String status;
    private String verificationStatus;
    private String checkInMethod;
    private LocalDateTime createdAt;
}
