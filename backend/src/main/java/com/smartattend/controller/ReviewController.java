package com.smartattend.controller;

import com.smartattend.model.dto.ApiResponse;
import com.smartattend.model.dto.CreateReviewRequest;
import com.smartattend.model.dto.ReviewDto;
import com.smartattend.model.entity.User;
import com.smartattend.model.enums.AttendanceStatus;
import com.smartattend.service.AuthService;
import com.smartattend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> submitReview(@Valid @RequestBody CreateReviewRequest request) {
        User currentUser = authService.getCurrentUser();
        ReviewDto review = reviewService.submitReview(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Dispute submitted successfully", review));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReviewDto>> resolveReview(
            @PathVariable Long id,
            @RequestParam String resolution,
            @RequestParam(required = false) AttendanceStatus newStatus) {
        User currentUser = authService.getCurrentUser();
        ReviewDto updated = reviewService.resolveReview(id, resolution, newStatus, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Review resolved successfully", updated));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getAllReviews() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getAllReviews()));
    }

    @GetMapping("/attendance/{attendanceId}")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getReviewsByAttendance(@PathVariable Long attendanceId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsByAttendance(attendanceId)));
    }
}
