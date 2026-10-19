package com.example.studentmanagement.repository;

import com.example.studentmanagement.model.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByStudentIdAndAttendanceDateAndSubject(Long studentId, LocalDate date, String subject);

    List<Attendance> findByAttendanceDateAndSubject(LocalDate date, String subject);

    List<Attendance> findBySubject(String subject);

    void deleteByStudentId(Long studentId);

    @Query("SELECT DISTINCT a.subject FROM Attendance a ORDER BY a.subject")
    List<String> findDistinctSubjects();
}
