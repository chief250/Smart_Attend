package com.smartattend.service;

import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.model.dto.AttendanceSessionDto;
import com.smartattend.model.dto.CreateSessionRequest;
import com.smartattend.model.entity.AttendanceSession;
import com.smartattend.model.entity.Course;
import com.smartattend.model.entity.Lecturer;
import com.smartattend.model.enums.SessionStatus;
import com.smartattend.repository.AttendanceSessionRepository;
import com.smartattend.repository.CourseRepository;
import com.smartattend.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceSessionService {

    private final AttendanceSessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final LecturerRepository lecturerRepository;

    @Transactional
    public AttendanceSessionDto createSession(CreateSessionRequest request, String lecturerEmail) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        Lecturer lecturer = lecturerRepository.findByUserEmail(lecturerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "email", lecturerEmail));

        AttendanceSession session = AttendanceSession.builder()
                .course(course)
                .lecturer(lecturer)
                .sessionDate(request.getSessionDate())
                .startTime(LocalDateTime.now())
                .lateThresholdMinutes(request.getLateThresholdMinutes())
                .status(SessionStatus.OPEN)
                .build();

        sessionRepository.save(session);
        return toDto(session);
    }

    @Transactional
    public AttendanceSessionDto closeSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));

        if (session.getStatus() != SessionStatus.OPEN) {
            throw new IllegalArgumentException("Session is not open");
        }

        session.setStatus(SessionStatus.CLOSED);
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);
        return toDto(session);
    }

    @Transactional
    public AttendanceSessionDto cancelSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));
        session.setStatus(SessionStatus.CANCELLED);
        sessionRepository.save(session);
        return toDto(session);
    }

    @Transactional(readOnly = true)
    public AttendanceSessionDto getSessionById(Long id) {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", id));
        return toDto(session);
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionDto> getAllSessions() {
        return sessionRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionDto> getSessionsByCourse(Long courseId) {
        return sessionRepository.findByCourseId(courseId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionDto> getSessionsByLecturer(Long lecturerId) {
        return sessionRepository.findByLecturerId(lecturerId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public AttendanceSessionDto toDto(AttendanceSession session) {
        return AttendanceSessionDto.builder()
                .id(session.getId())
                .courseId(session.getCourse().getId())
                .courseName(session.getCourse().getCourseName())
                .courseCode(session.getCourse().getCourseCode())
                .lecturerId(session.getLecturer().getId())
                .lecturerName(session.getLecturer().getUser().getFullName())
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .lateThresholdMinutes(session.getLateThresholdMinutes())
                .status(session.getStatus().name())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
