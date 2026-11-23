package com.smartattend.repository;

import com.smartattend.model.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    Optional<Lecturer> findByUserId(Long userId);
    Optional<Lecturer> findByStaffNumber(String staffNumber);
    boolean existsByStaffNumber(String staffNumber);

    @Query("SELECT l FROM Lecturer l JOIN l.user u WHERE u.email = :email")
    Optional<Lecturer> findByUserEmail(@Param("email") String email);
}
