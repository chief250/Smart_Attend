package com.smartattend.repository;

import com.smartattend.model.entity.AttendanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceReviewRepository extends JpaRepository<AttendanceReview, Long> {
    List<AttendanceReview> findByAttendanceId(Long attendanceId);
    List<AttendanceReview> findByReviewerId(Long reviewerId);
}
