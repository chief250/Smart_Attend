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
public class ReviewDto {
    private Long id;
    private Long attendanceId;
    private String studentName;
    private String courseName;
    private Long reviewerId;
    private String reviewerName;
    private String reason;
    private String resolution;
    private LocalDateTime createdAt;
}
