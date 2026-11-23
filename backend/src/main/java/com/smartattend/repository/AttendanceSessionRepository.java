package com.smartattend.repository;

import com.smartattend.model.entity.AttendanceSession;
import com.smartattend.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findByCourseId(Long courseId);
    List<AttendanceSession> findByLecturerId(Long lecturerId);
    List<AttendanceSession> findByCourseIdAndStatus(Long courseId, SessionStatus status);
    List<AttendanceSession> findBySessionDate(LocalDate sessionDate);
    List<AttendanceSession> findByCourseIdAndSessionDateBetween(Long courseId, LocalDate from, LocalDate to);
}
