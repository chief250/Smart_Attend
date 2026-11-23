package com.smartattend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceHistoryResponse {
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private int totalSessions;
    private int attendedSessions;
    private int lateSessions;
    private int absentSessions;
    private double attendancePercentage;
    private List<AttendanceRecordDto> records;
}
