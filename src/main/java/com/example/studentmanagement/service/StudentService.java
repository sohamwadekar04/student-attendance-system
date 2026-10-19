package com.example.studentmanagement.service;

import com.example.studentmanagement.exception.DuplicateRollNoException;
import com.example.studentmanagement.exception.StudentNotFoundException;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.AttendanceRepository;
import com.example.studentmanagement.repository.StudentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    public StudentService(StudentRepository studentRepository, AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public List<Student> getAll() {
        return studentRepository.findAllByOrderByRollNoAsc();
    }

    public Student create(Student student) {
        studentRepository.findByRollNoIgnoreCase(student.getRollNo()).ifPresent(existing -> {
            throw new DuplicateRollNoException(student.getRollNo());
        });
        student.setId(null);
        return studentRepository.save(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        // remove the student's attendance first so the foreign key is not violated
        attendanceRepository.deleteByStudentId(id);
        studentRepository.delete(student);
    }
}
