package com.example.studentmanagement.config;

import com.example.studentmanagement.model.Attendance;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.AttendanceRepository;
import com.example.studentmanagement.repository.StudentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
            // 1. Clear old sample data
            // --------------------------------------------------

            attendance.deleteAll();
            students.deleteAll();

            // --------------------------------------------------
            // 2. Create students
            // --------------------------------------------------

            List<Student> savedStudents = students.saveAll(List.of(

                    new Student(
                            "101",
                            "Nihar Vategavkar",
                            "nihar@example.com",
                            "SE-Comp-C"),

                    new Student(
                            "102",
                            "Soham Wadekar",
                            "soham@example.com",
                            "SE-Comp-C"),

                    new Student(
                            "103",
                            "Sakshi Bendke",
                            "sakshi@example.com",
                            "SE-Comp-C"),

                    new Student(
                            "104",
                            "Sahara Patil",
                            "aditya@example.com",
                            "SE-Comp-C"),

                    new Student(
                            "105",
                            "Neha Deshmukh",
                            "neha@example.com",
                            "SE-Comp-C"),

                    new Student(
                            "106",
                            "Riya Sharma",
                            "riya.sharma@example.com",
                            "SE-Comp-C")));

            // --------------------------------------------------
            // 3. Attendance configuration
            // --------------------------------------------------

            /*
             * 20 Java lectures for every student.
             *
             * 101 -> 20/20 = 100%
             * 102 -> 15/20 = 75%
             * 103 -> 15/20 = 75%
             * 104 -> 14/20 = 70%
             * 105 -> 5/20 = 25%
             * 106 -> 14/20 = 70%
             */

            int[] presentCount = {
                    20,
                    15,
                    15,
                    14,
                    5,
                    14
            };

            LocalDate startDate = LocalDate.of(2030, 1, 1);

            // --------------------------------------------------
            // 4. Prepare all attendance records
            // --------------------------------------------------

            List<Attendance> attendanceRecords = new ArrayList<>();

            for (int i = 0; i < savedStudents.size(); i++) {

                Student student = savedStudents.get(i);

                for (int lecture = 1; lecture <= 20; lecture++) {

                    boolean present = lecture <= presentCount[i];

                    Attendance record = new Attendance(
                            student,
                            startDate.plusDays(lecture - 1),
                            "Java",
                            present);

                    attendanceRecords.add(record);
                }
            }

            // --------------------------------------------------
            // 5. Save all attendance records together
            // --------------------------------------------------

            attendance.saveAll(attendanceRecords);

            // --------------------------------------------------
            // 6. Confirmation
            // --------------------------------------------------

            System.out.println("--------------------------------------");
            System.out.println("Sample attendance data loaded successfully.");
            System.out.println("Students: " + savedStudents.size());
            System.out.println("Java lectures per student: 20");
            System.out.println(
                    "Total attendance records: "
                            + attendanceRecords.size());
            System.out.println("--------------------------------------");
        };
    }
}