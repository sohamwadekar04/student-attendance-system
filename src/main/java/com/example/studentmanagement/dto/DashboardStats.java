package com.example.studentmanagement.dto;

import java.util.List;

public record DashboardStats(
        long totalStudents,
        double averageAttendance,
        long shortageCount,
        String highestAttendanceStudent,
        double highestAttendancePercentage,
        List<ReportRow> shortageStudents) {
}
