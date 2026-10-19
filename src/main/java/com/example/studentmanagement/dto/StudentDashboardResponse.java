package com.example.studentmanagement.dto;

import java.util.List;

public record StudentDashboardResponse(
        String rollNo,
        String name,
        String className,
        List<StudentSubjectAttendance> subjects,
        double overallPercentage,
        long totalClassesAll,
        long totalPresentAll) {
}
