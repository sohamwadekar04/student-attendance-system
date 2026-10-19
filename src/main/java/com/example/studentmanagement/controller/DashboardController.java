package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.DashboardStats;
import com.example.studentmanagement.dto.ReportRow;
import com.example.studentmanagement.service.AttendanceService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AttendanceService attendanceService;

    public DashboardController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/stats")
    public DashboardStats getStats() {
        List<ReportRow> report = attendanceService.report(null);
        long totalStudents = report.size();

        if (totalStudents == 0) {
            return new DashboardStats(0, 0.0, 0, "N/A", 0.0, List.of());
        }

        double sumPercentages = 0.0;
        long shortageCount = 0;
        ReportRow highest = report.get(0);

        for (ReportRow row : report) {
            sumPercentages += row.percentage();
            if (row.percentage() < 75.0 && row.totalClasses() > 0) {
                shortageCount++;
            }
            if (row.percentage() > highest.percentage()) {
                highest = row;
            }
        }

        double avgPct = Math.round((sumPercentages / totalStudents) * 10.0) / 10.0;
        List<ReportRow> shortageList = report.stream()
                .filter(r -> r.totalClasses() > 0 && r.percentage() < 75.0)
                .toList();

        return new DashboardStats(
                totalStudents,
                avgPct,
                shortageCount,
                highest.name(),
                highest.percentage(),
                shortageList
        );
    }
}
