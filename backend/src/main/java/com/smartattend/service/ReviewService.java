package com.smartattend.service;

import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.model.dto.CreateReviewRequest;
import com.smartattend.model.dto.ReviewDto;
import com.smartattend.model.entity.AttendanceRecord;
import com.smartattend.model.entity.AttendanceReview;
import com.smartattend.model.entity.User;
import com.smartattend.model.enums.AttendanceStatus;
import com.smartattend.repository.AttendanceRecordRepository;
import com.smartattend.repository.AttendanceReviewRepository;
import com.smartattend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final AttendanceReviewRepository reviewRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public ReviewDto submitReview(CreateReviewRequest request, Long reviewerUserId) {
        AttendanceRecord attendance = attendanceRecordRepository.findById(request.getAttendanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found: " + request.getAttendanceId()));

        User reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer user not found: " + reviewerUserId));

        AttendanceReview review = AttendanceReview.builder()
                .attendance(attendance)
                .reviewer(reviewer)
                .reason(request.getReason())
                .resolution(request.getResolution())
                .build();

        AttendanceReview saved = reviewRepository.save(review);
        auditService.logAction(reviewerUserId, "SUBMIT_ATTENDANCE_REVIEW", "ATTENDANCE_REVIEW", saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public ReviewDto resolveReview(Long reviewId, String resolution, AttendanceStatus newStatus, Long reviewerUserId) {
        AttendanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        review.setResolution(resolution);
        if (newStatus != null) {
            AttendanceRecord record = review.getAttendance();
            record.setStatus(newStatus);
            attendanceRecordRepository.save(record);
        }

        AttendanceReview updated = reviewRepository.save(review);
        auditService.logAction(reviewerUserId, "RESOLVE_ATTENDANCE_REVIEW", "ATTENDANCE_REVIEW", updated.getId());
        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByAttendance(Long attendanceId) {
        return reviewRepository.findByAttendanceId(attendanceId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ReviewDto mapToDto(AttendanceReview review) {
        return ReviewDto.builder()
                .id(review.getId())
                .attendanceId(review.getAttendance().getId())
                .studentName(review.getAttendance().getStudent().getUser().getFullName())
                .courseName(review.getAttendance().getSession().getCourse().getCourseName())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getFullName())
                .reason(review.getReason())
                .resolution(review.getResolution())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
