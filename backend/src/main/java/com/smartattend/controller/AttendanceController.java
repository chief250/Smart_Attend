package com.smartattend.controller;

import com.smartattend.model.dto.ApiResponse;
import com.smartattend.model.dto.AttendanceHistoryResponse;
import com.smartattend.model.dto.AttendanceRecordDto;
import com.smartattend.model.dto.QrScanRequest;
import com.smartattend.model.dto.StudentDto;
import com.smartattend.model.entity.User;
import com.smartattend.service.AttendanceService;
import com.smartattend.service.AuthService;
import com.smartattend.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AuthService authService;
    private final StudentService studentService;

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<AttendanceRecordDto>> checkIn(@Valid @RequestBody QrScanRequest request) {
        // If studentId is not supplied in request, resolve from authenticated user
        if (request.getStudentId() == null || request.getStudentId() <= 0) {
            User currentUser = authService.getCurrentUser();
            StudentDto studentDto = studentService.getStudentByUserId(currentUser.getId());
            request.setStudentId(studentDto.getId());
        }

        AttendanceRecordDto record = attendanceService.recordAttendance(request);
        return ResponseEntity.ok(ApiResponse.success("Attendance recorded successfully!", record));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> getMyHistory(@RequestParam(required = false) Long courseId) {
        User currentUser = authService.getCurrentUser();
        StudentDto student = studentService.getStudentByUserId(currentUser.getId());

        if (courseId != null) {
            AttendanceHistoryResponse history = attendanceService.getAttendanceHistory(student.getId(), courseId);
            return ResponseEntity.ok(ApiResponse.success(history.getRecords()));
        } else {
            List<AttendanceRecordDto> records = attendanceService.getAttendanceByStudent(student.getId());
            return ResponseEntity.ok(ApiResponse.success(records));
        }
    }

    @GetMapping("/history/course/{courseId}")
    public ResponseEntity<ApiResponse<AttendanceHistoryResponse>> getCourseAttendanceHistory(@PathVariable Long courseId) {
        User currentUser = authService.getCurrentUser();
        StudentDto student = studentService.getStudentByUserId(currentUser.getId());
        AttendanceHistoryResponse response = attendanceService.getAttendanceHistory(student.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/percentage")
    public ResponseEntity<ApiResponse<Double>> getAttendancePercentage(@RequestParam Long courseId) {
        User currentUser = authService.getCurrentUser();
        StudentDto student = studentService.getStudentByUserId(currentUser.getId());
        double percentage = attendanceService.getAttendancePercentage(student.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(percentage));
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> getSessionAttendance(@PathVariable Long sessionId) {
        List<AttendanceRecordDto> records = attendanceService.getAttendanceBySession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }
}

