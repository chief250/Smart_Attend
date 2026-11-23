package com.smartattend.repository;

import com.smartattend.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByStudentNumber(String studentNumber);
    boolean existsByStudentNumber(String studentNumber);

    @Query("SELECT s FROM Student s JOIN s.user u WHERE u.email = :email")
    Optional<Student> findByUserEmail(@Param("email") String email);
}
