package com.example.studentmanagement.dto;

import java.time.LocalDate;

public record AttendanceResponse(
        Long studentId,
        String rollNo,
        String name,
        LocalDate date,
        String subject,
        boolean present) {
}
