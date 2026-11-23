package com.smartattend.controller;

import com.smartattend.model.dto.ApiResponse;
import com.smartattend.model.dto.CourseDto;
import com.smartattend.model.dto.LecturerDto;
import com.smartattend.model.entity.User;
import com.smartattend.service.AuthService;
import com.smartattend.service.CourseService;
import com.smartattend.service.LecturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LecturerController {

    private final LecturerService lecturerService;
    private final CourseService courseService;
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LecturerDto>> getMyProfile() {
        User currentUser = authService.getCurrentUser();
        LecturerDto lecturer = lecturerService.getLecturerByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(lecturer));
    }

    @GetMapping("/me/courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getMyCourses() {
        User currentUser = authService.getCurrentUser();
        LecturerDto lecturer = lecturerService.getLecturerByUserId(currentUser.getId());
        List<CourseDto> courses = courseService.getCoursesByLecturer(lecturer.getId());
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<ApiResponse<LecturerDto>> getLecturerById(@PathVariable Long id) {
        LecturerDto lecturer = lecturerService.getLecturerById(id);
        return ResponseEntity.ok(ApiResponse.success(lecturer));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LecturerDto>>> getAllLecturers() {
        List<LecturerDto> lecturers = lecturerService.getAllLecturers();
        return ResponseEntity.ok(ApiResponse.success(lecturers));
    }
}
