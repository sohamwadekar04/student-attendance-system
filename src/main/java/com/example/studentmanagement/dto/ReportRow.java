package com.example.studentmanagement.dto;

public record ReportRow(
        Long studentId,
        String rollNo,
        String name,
        long totalClasses,
        long presentCount,
        double percentage) {
}
