package com.smartattend.service;

import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.model.dto.StudentDto;
import com.smartattend.model.dto.UserDto;
import com.smartattend.model.entity.Student;
import com.smartattend.model.entity.User;
import com.smartattend.model.enums.UserRole;
import com.smartattend.model.enums.UserStatus;
import com.smartattend.repository.StudentRepository;
import com.smartattend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return toDto(student);
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentByEmail(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", email));
        return toDto(student);
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userId));
        return toDto(student);
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentDto createStudent(UserDto userDto, String studentNumber, String program, String classGroup) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + userDto.getEmail());
        }
        if (studentRepository.existsByStudentNumber(studentNumber)) {
            throw new IllegalArgumentException("Student number already in use: " + studentNumber);
        }

        User user = User.builder()
                .fullName(userDto.getFullName())
                .email(userDto.getEmail())
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .role(UserRole.STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        Student student = Student.builder()
                .user(user)
                .studentNumber(studentNumber)
                .program(program)
                .classGroup(classGroup)
                .build();
        studentRepository.save(student);

        return toDto(student);
    }

    @Transactional
    public StudentDto updateStudent(Long id, StudentDto dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (dto.getStudentNumber() != null && !dto.getStudentNumber().equals(student.getStudentNumber())) {
            if (studentRepository.existsByStudentNumber(dto.getStudentNumber())) {
                throw new IllegalArgumentException("Student number already in use");
            }
            student.setStudentNumber(dto.getStudentNumber());
        }
        if (dto.getProgram() != null) student.setProgram(dto.getProgram());
        if (dto.getClassGroup() != null) student.setClassGroup(dto.getClassGroup());

        if (dto.getFullName() != null) student.getUser().setFullName(dto.getFullName());
        if (dto.getStatus() != null) student.getUser().setStatus(UserStatus.valueOf(dto.getStatus()));

        userRepository.save(student.getUser());
        studentRepository.save(student);
        return toDto(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        studentRepository.delete(student);
    }

    public StudentDto toDto(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .userId(student.getUser().getId())
                .fullName(student.getUser().getFullName())
                .email(student.getUser().getEmail())
                .studentNumber(student.getStudentNumber())
                .program(student.getProgram())
                .classGroup(student.getClassGroup())
                .status(student.getUser().getStatus().name())
                .build();
    }
}
