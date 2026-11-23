package com.smartattend.service;

import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.model.dto.EnrollmentDto;
import com.smartattend.model.entity.Course;
import com.smartattend.model.entity.Enrollment;
import com.smartattend.model.entity.Student;
import com.smartattend.repository.CourseRepository;
import com.smartattend.repository.EnrollmentRepository;
import com.smartattend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public EnrollmentDto enrollStudent(Long studentId, Long courseId, String academicPeriod) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicPeriod(studentId, courseId, academicPeriod)) {
            throw new IllegalArgumentException("Student is already enrolled in this course for period: " + academicPeriod);
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .academicPeriod(academicPeriod)
                .build();

        enrollmentRepository.save(enrollment);
        return toDto(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void unenrollStudent(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));
        enrollmentRepository.delete(enrollment);
    }

    public EnrollmentDto toDto(Enrollment enrollment) {
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getUser().getFullName())
                .studentNumber(enrollment.getStudent().getStudentNumber())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getCourseName())
                .courseCode(enrollment.getCourse().getCourseCode())
                .academicPeriod(enrollment.getAcademicPeriod())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}
