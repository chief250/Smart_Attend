package com.smartattend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private String reportType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long entityId;
    private String entityName;
    private int totalSessions;
    private int totalStudents;
    private double averageAttendancePercentage;
    private List<AttendanceRecordDto> records;
}
