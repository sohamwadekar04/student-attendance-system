package com.example.studentmanagement.dto;

public record StudentSubjectAttendance(
        String subject,
        long totalClasses,
        long presentCount,
        double percentage,
        boolean shortage) {
}
