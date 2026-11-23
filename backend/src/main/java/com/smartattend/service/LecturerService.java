package com.smartattend.service;

import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.model.dto.LecturerDto;
import com.smartattend.model.dto.UserDto;
import com.smartattend.model.entity.Lecturer;
import com.smartattend.model.entity.User;
import com.smartattend.model.enums.UserRole;
import com.smartattend.model.enums.UserStatus;
import com.smartattend.repository.LecturerRepository;
import com.smartattend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerService {

    private final LecturerRepository lecturerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LecturerDto getLecturerById(Long id) {
        Lecturer lecturer = lecturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", id));
        return toDto(lecturer);
    }

    @Transactional(readOnly = true)
    public LecturerDto getLecturerByEmail(String email) {
        Lecturer lecturer = lecturerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "email", email));
        return toDto(lecturer);
    }

    @Transactional(readOnly = true)
    public LecturerDto getLecturerByUserId(Long userId) {
        Lecturer lecturer = lecturerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "userId", userId));
        return toDto(lecturer);
    }

    @Transactional(readOnly = true)
    public List<LecturerDto> getAllLecturers() {
        return lecturerRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LecturerDto createLecturer(UserDto userDto, String staffNumber, String department) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + userDto.getEmail());
        }
        if (lecturerRepository.existsByStaffNumber(staffNumber)) {
            throw new IllegalArgumentException("Staff number already in use: " + staffNumber);
        }

        User user = User.builder()
                .fullName(userDto.getFullName())
                .email(userDto.getEmail())
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .role(UserRole.LECTURER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        Lecturer lecturer = Lecturer.builder()
                .user(user)
                .staffNumber(staffNumber)
                .department(department)
                .build();
        lecturerRepository.save(lecturer);

        return toDto(lecturer);
    }

    @Transactional
    public LecturerDto updateLecturer(Long id, LecturerDto dto) {
        Lecturer lecturer = lecturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", id));

        if (dto.getStaffNumber() != null && !dto.getStaffNumber().equals(lecturer.getStaffNumber())) {
            if (lecturerRepository.existsByStaffNumber(dto.getStaffNumber())) {
                throw new IllegalArgumentException("Staff number already in use");
            }
            lecturer.setStaffNumber(dto.getStaffNumber());
        }
        if (dto.getDepartment() != null) lecturer.setDepartment(dto.getDepartment());
        if (dto.getFullName() != null) lecturer.getUser().setFullName(dto.getFullName());
        if (dto.getStatus() != null) lecturer.getUser().setStatus(UserStatus.valueOf(dto.getStatus()));

        userRepository.save(lecturer.getUser());
        lecturerRepository.save(lecturer);
        return toDto(lecturer);
    }

    @Transactional
    public void deleteLecturer(Long id) {
        Lecturer lecturer = lecturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", id));
        lecturerRepository.delete(lecturer);
    }

    public LecturerDto toDto(Lecturer lecturer) {
        return LecturerDto.builder()
                .id(lecturer.getId())
                .userId(lecturer.getUser().getId())
                .fullName(lecturer.getUser().getFullName())
                .email(lecturer.getUser().getEmail())
                .staffNumber(lecturer.getStaffNumber())
                .department(lecturer.getDepartment())
                .status(lecturer.getUser().getStatus().name())
                .build();
    }
}
