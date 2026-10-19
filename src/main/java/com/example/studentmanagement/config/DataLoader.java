package com.example.studentmanagement.config;

import com.example.studentmanagement.model.Attendance;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.AttendanceRepository;
import com.example.studentmanagement.repository.StudentRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadSampleData(
            StudentRepository students,
            AttendanceRepository attendance) {

        return args -> {

            // --------------------------------------------------
            // 1. Create students only if they do not exist
            // --------------------------------------------------
            Map<String, Student> studentMap = new HashMap<>();

            List<Student> existingStudents = students.findAll();

            for (Student student : existingStudents) {
                studentMap.put(student.getRollNo(), student);
            }

            String[][] studentData = {
                    { "101", "Nihar Vategavkar", "nihar@example.com", "SE-Comp-C" },
                    { "102", "Soham Wadekar", "soham@example.com", "SE-Comp-C" },
                    { "103", "Sakshi Bendke", "sakshi@example.com", "SE-Comp-C" },
                    { "104", "Sahara Patil", "aditya@example.com", "SE-Comp-C" },
                    { "105", "Neha Deshmukh", "neha@example.com", "SE-Comp-C" },
                    { "106", "Riya Sharma", "riya.sharma@example.com", "SE-Comp-C" }
            };

            for (String[] data : studentData) {

                Student student = studentMap.get(data[0]);

                if (student == null) {
                    student = students.save(
                            new Student(
                                    data[0],
                                    data[1],
                                    data[2],
                                    data[3]));

                    studentMap.put(data[0], student);

                } else if (data[0].equals("104")
                        && !student.getName().equals("Sahara Patil")) {

                    // Change existing Roll 104 name to Sahara Patil
                    student.setName("Sahara Patil");
                    students.save(student);
                }
            }

            // --------------------------------------------------
            // 2. Desired attendance percentages
            // --------------------------------------------------
            Map<String, Integer> targetPresent = Map.of(
                    "101", 100,
                    "102", 75,
                    "103", 75,
                    "104", 72,
                    "105", 25,
                    "106", 70);

            // --------------------------------------------------
            // 3. Get all existing attendance
            // --------------------------------------------------
            List<Attendance> allAttendance = attendance.findAll();

            // --------------------------------------------------
            // 4. Create/complete Java attendance
            // --------------------------------------------------
            for (String rollNo : targetPresent.keySet()) {

                Student student = studentMap.get(rollNo);

                if (student == null) {
                    continue;
                }

                int requiredPresent = targetPresent.get(rollNo);

                List<Attendance> javaAttendance = new ArrayList<>();

                for (Attendance a : allAttendance) {
                    if (a.getStudent().getId().equals(student.getId())
                            && "Java".equals(a.getSubject())) {
                        javaAttendance.add(a);
                    }
                }

                javaAttendance.sort(
                        Comparator.comparing(Attendance::getAttendanceDate));

                // --------------------------------------------------
                // Keep existing records and modify their status
                // --------------------------------------------------
                int existingCount = javaAttendance.size();

                for (int i = 0; i < existingCount; i++) {
                    javaAttendance.get(i).setPresent(i < requiredPresent);
                }

                attendance.saveAll(javaAttendance);

                // --------------------------------------------------
                // Add missing lectures until total reaches 100
                // --------------------------------------------------
                Set<LocalDate> usedDates = new HashSet<>();

                for (Attendance a : javaAttendance) {
                    usedDates.add(a.getAttendanceDate());
                }

                int currentCount = javaAttendance.size();
                int currentPresent = Math.min(requiredPresent, currentCount);

                LocalDate date = LocalDate.of(2030, 1, 1);

                List<Attendance> newAttendance = new ArrayList<>();

                while (currentCount < 100) {

                    if (!usedDates.contains(date)) {

                        boolean present = currentPresent < requiredPresent;

                        newAttendance.add(
                                new Attendance(
                                        student,
                                        date,
                                        "Java",
                                        present));

                        usedDates.add(date);
                        currentCount++;

                        if (present) {
                            currentPresent++;
                        }
                    }

                    date = date.plusDays(1);
                }

                attendance.saveAll(newAttendance);
            }
        };
    }
}