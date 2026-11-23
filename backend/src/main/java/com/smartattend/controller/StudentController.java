package com.smartattend.controller;

import com.smartattend.model.dto.ApiResponse;
import com.smartattend.model.dto.EnrollmentDto;
import com.smartattend.model.dto.StudentDto;
import com.smartattend.model.entity.User;
import com.smartattend.service.AuthService;
import com.smartattend.service.EnrollmentService;
import com.smartattend.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentDto>> getMyProfile() {
        User currentUser = authService.getCurrentUser();
        StudentDto student = studentService.getStudentByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    @GetMapping("/me/courses")
    public ResponseEntity<ApiResponse<List<EnrollmentDto>>> getMyCourses() {
        User currentUser = authService.getCurrentUser();
        StudentDto student = studentService.getStudentByUserId(currentUser.getId());
        List<EnrollmentDto> courses = enrollmentService.getEnrollmentsByStudent(student.getId());
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<ApiResponse<StudentDto>> getStudentById(@PathVariable Long id) {
        StudentDto student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<ApiResponse<List<StudentDto>>> getAllStudents() {
        List<StudentDto> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StudentDto>> updateMyProfile(@RequestBody StudentDto updateDto) {
        User currentUser = authService.getCurrentUser();
        StudentDto currentStudent = studentService.getStudentByUserId(currentUser.getId());
        StudentDto updated = studentService.updateStudent(currentStudent.getId(), updateDto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }
}
