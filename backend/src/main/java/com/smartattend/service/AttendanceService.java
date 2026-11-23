package com.smartattend.service;

import com.smartattend.exception.DuplicateAttendanceException;
import com.smartattend.exception.FaceVerificationException;
import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.model.dto.AttendanceHistoryResponse;
import com.smartattend.model.dto.AttendanceRecordDto;
import com.smartattend.model.dto.QrScanRequest;
import com.smartattend.model.entity.*;
import com.smartattend.model.enums.AttendanceStatus;
import com.smartattend.model.enums.SessionStatus;
import com.smartattend.model.enums.VerificationStatus;
import com.smartattend.repository.AttendanceRecordRepository;
import com.smartattend.repository.AttendanceSessionRepository;
import com.smartattend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final QrTokenService qrTokenService;

    @Transactional
    public AttendanceRecordDto recordAttendance(QrScanRequest request) {
        // 1. Face verification check
        if (Boolean.FALSE.equals(request.getFaceVerified())) {
            throw new FaceVerificationException("Face verification failed. Attendance not recorded.");
        }

        // 2. Validate QR token
        QrToken qrToken = qrTokenService.validateToken(request.getQrToken());
        AttendanceSession session = qrToken.getSession();

        // 3. Check session is open
        if (session.getStatus() != SessionStatus.OPEN) {
            throw new IllegalArgumentException("Attendance session is not open.");
        }

        // 4. Find student
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        // 5. Duplicate check
        if (attendanceRecordRepository.existsByStudentIdAndSessionId(student.getId(), session.getId())) {
            throw new DuplicateAttendanceException("Attendance already recorded for this student in this session.");
        }

        // 6. Determine attendance status (PRESENT or LATE)
        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status;
        if (now.isAfter(session.getStartTime().plusMinutes(session.getLateThresholdMinutes()))) {
            status = AttendanceStatus.LATE;
        } else {
            status = AttendanceStatus.PRESENT;
        }

        // 7. Create attendance record
        AttendanceRecord record = AttendanceRecord.builder()
                .student(student)
                .session(session)
                .checkInTime(now)
                .status(status)
                .verificationStatus(VerificationStatus.VERIFIED)
                .checkInMethod("QR_FACE")
                .build();

        attendanceRecordRepository.save(record);

        log.info("Attendance recorded: student={}, session={}, status={}", student.getId(), session.getId(), status);
        return toDto(record);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> getAttendanceByStudent(Long studentId) {
        return attendanceRecordRepository.findByStudentId(studentId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> getAttendanceBySession(Long sessionId) {
        return attendanceRecordRepository.findBySessionId(sessionId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceHistoryResponse getAttendanceHistory(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        List<AttendanceRecord> records = attendanceRecordRepository.findByStudentIdAndCourseId(studentId, courseId);
        int totalSessions = sessionRepository.findByCourseId(courseId).size();
        long presentCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long lateCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        long absentCount = totalSessions - presentCount - lateCount;
        double percentage = totalSessions > 0
                ? (double) (presentCount + lateCount) / totalSessions * 100.0
                : 0.0;

        String courseName = records.isEmpty() ? "" : records.get(0).getSession().getCourse().getCourseName();
        String courseCode = records.isEmpty() ? "" : records.get(0).getSession().getCourse().getCourseCode();

        return AttendanceHistoryResponse.builder()
                .studentId(studentId)
                .studentName(student.getUser().getFullName())
                .studentNumber(student.getStudentNumber())
                .courseId(courseId)
                .courseName(courseName)
                .courseCode(courseCode)
                .totalSessions(totalSessions)
                .attendedSessions((int) presentCount)
                .lateSessions((int) lateCount)
                .absentSessions((int) absentCount)
                .attendancePercentage(Math.round(percentage * 100.0) / 100.0)
                .records(records.stream().map(this::toDto).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public double getAttendancePercentage(Long studentId, Long courseId) {
        int totalSessions = sessionRepository.findByCourseId(courseId).size();
        if (totalSessions == 0) return 0.0;

        long present = attendanceRecordRepository.countByStudentIdAndCourseIdAndStatus(studentId, courseId, AttendanceStatus.PRESENT);
        long late = attendanceRecordRepository.countByStudentIdAndCourseIdAndStatus(studentId, courseId, AttendanceStatus.LATE);
        return Math.round((double)(present + late) / totalSessions * 10000.0) / 100.0;
    }

    public AttendanceRecordDto toDto(AttendanceRecord record) {
        return AttendanceRecordDto.builder()
                .id(record.getId())
                .studentId(record.getStudent().getId())
                .studentName(record.getStudent().getUser().getFullName())
                .studentNumber(record.getStudent().getStudentNumber())
                .sessionId(record.getSession().getId())
                .courseName(record.getSession().getCourse().getCourseName())
                .courseCode(record.getSession().getCourse().getCourseCode())
                .checkInTime(record.getCheckInTime())
                .status(record.getStatus().name())
                .verificationStatus(record.getVerificationStatus().name())
                .checkInMethod(record.getCheckInMethod())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
