package com.example.studentmanagement.repository;

import com.example.studentmanagement.model.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRollNoIgnoreCase(String rollNo);

    List<Student> findAllByOrderByRollNoAsc();
}
