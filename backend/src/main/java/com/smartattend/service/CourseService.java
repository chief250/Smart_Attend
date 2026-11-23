package com.smartattend.service;

import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.model.dto.CourseDto;
import com.smartattend.model.entity.Course;
import com.smartattend.model.entity.Lecturer;
import com.smartattend.model.enums.UserStatus;
import com.smartattend.repository.CourseRepository;
import com.smartattend.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final LecturerRepository lecturerRepository;

    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return toDto(course);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByLecturer(Long lecturerId) {
        return courseRepository.findByLecturerId(lecturerId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public CourseDto createCourse(CourseDto dto) {
        if (courseRepository.existsByCourseCode(dto.getCourseCode())) {
            throw new IllegalArgumentException("Course code already in use: " + dto.getCourseCode());
        }

        Lecturer lecturer = null;
        if (dto.getLecturerId() != null) {
            lecturer = lecturerRepository.findById(dto.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", dto.getLecturerId()));
        }

        Course course = Course.builder()
                .courseCode(dto.getCourseCode())
                .courseName(dto.getCourseName())
                .description(dto.getDescription())
                .lecturer(lecturer)
                .academicPeriod(dto.getAcademicPeriod())
                .status(dto.getStatus() != null ? UserStatus.valueOf(dto.getStatus()) : UserStatus.ACTIVE)
                .build();

        courseRepository.save(course);
        return toDto(course);
    }

    @Transactional
    public CourseDto updateCourse(Long id, CourseDto dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        if (dto.getCourseCode() != null && !dto.getCourseCode().equals(course.getCourseCode())) {
            if (courseRepository.existsByCourseCode(dto.getCourseCode())) {
                throw new IllegalArgumentException("Course code already in use");
            }
            course.setCourseCode(dto.getCourseCode());
        }
        if (dto.getCourseName() != null) course.setCourseName(dto.getCourseName());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getAcademicPeriod() != null) course.setAcademicPeriod(dto.getAcademicPeriod());
        if (dto.getStatus() != null) course.setStatus(UserStatus.valueOf(dto.getStatus()));
        if (dto.getLecturerId() != null) {
            Lecturer lecturer = lecturerRepository.findById(dto.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", dto.getLecturerId()));
            course.setLecturer(lecturer);
        }

        courseRepository.save(course);
        return toDto(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        courseRepository.delete(course);
    }

    public CourseDto toDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .lecturerId(course.getLecturer() != null ? course.getLecturer().getId() : null)
                .lecturerName(course.getLecturer() != null ? course.getLecturer().getUser().getFullName() : null)
                .academicPeriod(course.getAcademicPeriod())
                .status(course.getStatus().name())
                .build();
    }
}
