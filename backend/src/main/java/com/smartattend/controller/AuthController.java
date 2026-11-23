package com.smartattend.controller;

import com.smartattend.model.dto.ApiResponse;
import com.smartattend.model.dto.LoginRequest;
import com.smartattend.model.dto.LoginResponse;
import com.smartattend.model.dto.StudentDto;
import com.smartattend.model.dto.UserDto;
import com.smartattend.model.enums.UserRole;
import com.smartattend.service.AdminService;
import com.smartattend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<StudentDto>> registerStudent(@Valid @RequestBody UserDto userDto,
                                                                   @RequestParam(defaultValue = "UNKNOWN") String studentNumber,
                                                                   @RequestParam(defaultValue = "Computer Science") String program,
                                                                   @RequestParam(defaultValue = "Section A") String classGroup) {
        userDto.setRole(UserRole.STUDENT);
        StudentDto created = adminService.createStudentUser(userDto, studentNumber, program, classGroup);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", created));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
