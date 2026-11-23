package com.smartattend.controller;

import com.smartattend.model.dto.ApiResponse;
import com.smartattend.model.dto.AttendanceSessionDto;
import com.smartattend.model.dto.CreateSessionRequest;
import com.smartattend.model.dto.StudentDto;
import com.smartattend.model.entity.User;
import com.smartattend.model.enums.UserRole;
import com.smartattend.repository.AttendanceRecordRepository;
import com.smartattend.service.AttendanceSessionService;
import com.smartattend.service.AuthService;
import com.smartattend.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttendanceSessionController {

    private final AttendanceSessionService sessionService;
    private final AuthService authService;
    private final StudentService studentService;
    private final AttendanceRecordRepository attendanceRecordRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceSessionDto>> createSession(@Valid @RequestBody CreateSessionRequest request) {
        User user = authService.getCurrentUser();
        AttendanceSessionDto session = sessionService.createSession(request, user.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Session created successfully", session));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceSessionDto>> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.getSessionById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceSessionDto>>> getAllSessions(@RequestParam(required = false) Long courseId,
                                                                                  @RequestParam(required = false) Long lecturerId) {
        List<AttendanceSessionDto> sessions;
        if (courseId != null) {
            sessions = sessionService.getSessionsByCourse(courseId);
        } else if (lecturerId != null) {
            sessions = sessionService.getSessionsByLecturer(lecturerId);
        } else {
            sessions = sessionService.getAllSessions();
        }
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AttendanceSessionDto>>> getTodaySessions() {
        LocalDate today = LocalDate.now();
        List<AttendanceSessionDto> todaySessions = sessionService.getAllSessions().stream()
                .filter(s -> today.equals(s.getSessionDate()))
                .collect(Collectors.toList());

        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == UserRole.STUDENT) {
                StudentDto student = studentService.getStudentByUserId(currentUser.getId());
                for (AttendanceSessionDto s : todaySessions) {
                    attendanceRecordRepository.findByStudentIdAndSessionId(student.getId(), s.getId())
                            .ifPresent(record -> {
                                s.setAttended(true);
                                s.setAttendanceStatus(record.getStatus().name());
                            });
                }
            }
        } catch (Exception ignored) {}

        return ResponseEntity.ok(ApiResponse.success(todaySessions));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceSessionDto>> closeSession(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Session closed", sessionService.closeSession(id)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceSessionDto>> cancelSession(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Session cancelled", sessionService.cancelSession(id)));
    }
}