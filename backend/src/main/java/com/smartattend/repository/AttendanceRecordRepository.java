package com.smartattend.repository;

import com.smartattend.model.entity.AttendanceRecord;
import com.smartattend.model.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByStudentIdAndSessionId(Long studentId, Long sessionId);
    boolean existsByStudentIdAndSessionId(Long studentId, Long sessionId);
    List<AttendanceRecord> findByStudentId(Long studentId);
    List<AttendanceRecord> findBySessionId(Long sessionId);

    @Query("SELECT ar FROM AttendanceRecord ar JOIN ar.session s WHERE s.course.id = :courseId")
    List<AttendanceRecord> findBySessionCourseId(@Param("courseId") Long courseId);

    @Query("SELECT ar FROM AttendanceRecord ar " +
           "JOIN ar.session s " +
           "WHERE ar.student.id = :studentId AND s.course.id = :courseId")
    List<AttendanceRecord> findByStudentIdAndCourseId(@Param("studentId") Long studentId,
                                                       @Param("courseId") Long courseId);

    @Query("SELECT ar FROM AttendanceRecord ar " +
           "JOIN ar.session s " +
           "WHERE ar.student.id = :studentId AND s.course.id = :courseId " +
           "AND s.sessionDate BETWEEN :from AND :to")
    List<AttendanceRecord> findByStudentIdAndCourseIdAndDateRange(@Param("studentId") Long studentId,
                                                                   @Param("courseId") Long courseId,
                                                                   @Param("from") LocalDate from,
                                                                   @Param("to") LocalDate to);

    @Query("SELECT ar FROM AttendanceRecord ar " +
           "JOIN ar.session s " +
           "WHERE s.course.id = :courseId AND s.sessionDate BETWEEN :from AND :to")
    List<AttendanceRecord> findByCourseIdAndDateRange(@Param("courseId") Long courseId,
                                                       @Param("from") LocalDate from,
                                                       @Param("to") LocalDate to);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar " +
           "JOIN ar.session s " +
           "WHERE ar.student.id = :studentId AND s.course.id = :courseId AND ar.status = :status")
    long countByStudentIdAndCourseIdAndStatus(@Param("studentId") Long studentId,
                                               @Param("courseId") Long courseId,
                                               @Param("status") AttendanceStatus status);
}
