package com.smartattend.controller;

import com.smartattend.model.dto.*;
import com.smartattend.model.enums.UserStatus;
import com.smartattend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ReportService reportService;
    private final AuditService auditService;
    private final AuthService authService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(id)));
    }

    @PostMapping("/users/student")
    public ResponseEntity<ApiResponse<StudentDto>> createStudent(@Valid @RequestBody UserDto userDto,
                                                                 @RequestParam String studentNumber,
                                                                 @RequestParam(defaultValue = "Computer Science") String program,
                                                                 @RequestParam(defaultValue = "Section A") String classGroup) {
        StudentDto created = adminService.createStudentUser(userDto, studentNumber, program, classGroup);
        auditService.logAction(authService.getCurrentUser().getId(), "CREATE_STUDENT", "STUDENT", created.getId());
        return ResponseEntity.ok(ApiResponse.success("Student created successfully", created));
    }

    @PostMapping("/users/lecturer")
    public ResponseEntity<ApiResponse<LecturerDto>> createLecturer(@Valid @RequestBody UserDto userDto,
                                                                   @RequestParam String staffNumber,
                                                                   @RequestParam(defaultValue = "Computer Science") String department) {
        LecturerDto created = adminService.createLecturerUser(userDto, staffNumber, department);
        auditService.logAction(authService.getCurrentUser().getId(), "CREATE_LECTURER", "LECTURER", created.getId());
        return ResponseEntity.ok(ApiResponse.success("Lecturer created successfully", created));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        UserDto updated = adminService.updateUserStatus(id, status);
        auditService.logAction(authService.getCurrentUser().getId(), "UPDATE_USER_STATUS", "USER", id);
        return ResponseEntity.ok(ApiResponse.success("User status updated", updated));
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getAllCourses() {
        return ResponseEntity.ok(ApiResponse.success(courseService.getAllCourses()));
    }

    @PostMapping("/courses")
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto created = courseService.createCourse(courseDto);
        auditService.logAction(authService.getCurrentUser().getId(), "CREATE_COURSE", "COURSE", created.getId());
        return ResponseEntity.ok(ApiResponse.success("Course created successfully", created));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        CourseDto updated = courseService.updateCourse(id, courseDto);
        auditService.logAction(authService.getCurrentUser().getId(), "UPDATE_COURSE", "COURSE", id);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updated));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        auditService.logAction(authService.getCurrentUser().getId(), "DELETE_COURSE", "COURSE", id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }

    @PostMapping("/enrollments")
    public ResponseEntity<ApiResponse<EnrollmentDto>> enrollStudent(@RequestParam Long studentId,
                                                                    @RequestParam Long courseId,
                                                                    @RequestParam(defaultValue = "Semester 1 • 2026") String academicPeriod) {
        EnrollmentDto enrolled = enrollmentService.enrollStudent(studentId, courseId, academicPeriod);
        auditService.logAction(authService.getCurrentUser().getId(), "ENROLL_STUDENT", "ENROLLMENT", enrolled.getId());
        return ResponseEntity.ok(ApiResponse.success("Student enrolled successfully", enrolled));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        Map<String, Object> stats = reportService.getInstitutionSummary();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
