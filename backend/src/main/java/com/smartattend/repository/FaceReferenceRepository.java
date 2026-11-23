package com.smartattend.repository;

import com.smartattend.model.entity.FaceReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FaceReferenceRepository extends JpaRepository<FaceReference, Long> {
    Optional<FaceReference> findByStudentId(Long studentId);
    boolean existsByStudentId(Long studentId);
}
