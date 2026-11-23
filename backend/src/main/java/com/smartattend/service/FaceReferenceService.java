package com.smartattend.service;

import com.smartattend.exception.ResourceNotFoundException;
import com.smartattend.model.entity.FaceReference;
import com.smartattend.model.entity.Student;
import com.smartattend.repository.FaceReferenceRepository;
import com.smartattend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaceReferenceService {

    private final FaceReferenceRepository faceReferenceRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public FaceReference registerFaceReference(Long studentId, byte[] referenceData) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        FaceReference faceReference = faceReferenceRepository.findByStudentId(studentId)
                .orElse(FaceReference.builder()
                        .student(student)
                        .build());

        faceReference.setReferenceData(referenceData);
        log.info("Face reference registered successfully for student ID: {}", studentId);
        return faceReferenceRepository.save(faceReference);
    }

    @Transactional(readOnly = true)
    public FaceReference getFaceReference(Long studentId) {
        return faceReferenceRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Face reference not found for student ID: " + studentId));
    }

    @Transactional(readOnly = true)
    public boolean hasFaceRegistered(Long studentId) {
        return faceReferenceRepository.existsByStudentId(studentId);
    }
}
