package com.example.studentmanagement.config;

import com.example.studentmanagement.model.Attendance;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.AttendanceRepository;
import com.example.studentmanagement.repository.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Loads sample students and a few days of attendance so the report has data to
 * show.
 */
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadSampleData(StudentRepository students, AttendanceRepository attendance) {
        return args -> {
            attendance.deleteAll();
            students.deleteAll();
            List<Student> saved = students.saveAll(List.of(
                    new Student("101", "Nihar Vategavkar", "nihar@example.com", "SE-Comp-C"),
                    new Student("102", "Soham Wadekar", "soham@example.com", "SE-Comp-C"),
                    new Student("103", "Sakshi Bendke", "sakshi@example.com", "SE-Comp-C"),
                    new Student("104", "Aditya Joshi", "aditya@example.com", "SE-Comp-C"),
                    new Student("105", "Neha Deshmukh", "neha@example.com", "SE-Comp-C")));

            int[] absences = {0, 1, 1, 2, 3}; // days absent (out of 4) for each student
            LocalDate today = LocalDate.now();
            for (int day = 1; day <= 4; day++) {
                LocalDate date = today.minusDays(day);
                for (int i = 0; i < saved.size(); i++) {
                    boolean present = day > absences[i];
                    attendance.save(new Attendance(saved.get(i), date, "Java", present));
                }
            }
        };
    }
}
