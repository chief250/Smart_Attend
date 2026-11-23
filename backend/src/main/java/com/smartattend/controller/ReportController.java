package com.smartattend.controller;

import com.smartattend.model.dto.ApiResponse;
import com.smartattend.model.dto.ReportDto;
import com.smartattend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/student/{id}")
    public ResponseEntity<ApiResponse<ReportDto>> getStudentReport(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ReportDto report = reportService.getStudentReport(id, from, to);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/course/{id}")
    public ResponseEntity<ApiResponse<ReportDto>> getCourseReport(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ReportDto report = reportService.getCourseReport(id, from, to);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInstitutionSummary() {
        Map<String, Object> summary = reportService.getInstitutionSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
