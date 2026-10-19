package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.StudentDashboardResponse;
import com.example.studentmanagement.dto.StudentSubjectAttendance;
import com.example.studentmanagement.model.Attendance;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.AttendanceRepository;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Read-only API for the student portal. Students can only see their own attendance.
 */
@RestController
@RequestMapping("/api/student")
public class StudentPortalController {

    public static final double SHORTAGE_THRESHOLD = 75.0;

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;

    public StudentPortalController(StudentRepository studentRepository,
                                   AttendanceRepository attendanceRepository,
                                   AttendanceService attendanceService) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceService = attendanceService;
    }

    /** Returns the logged-in student's full dashboard data. */
    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> dashboard(HttpServletRequest request) {
        String rollNo = getStudentRollNo(request);
        Student student = studentRepository.findByRollNoIgnoreCase(rollNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        // All attendance records for this student
        List<Attendance> records = attendanceRepository.findAll().stream()
                .filter(a -> a.getStudent().getId().equals(student.getId()))
                .toList();

        // Build per-subject counts
        Map<String, long[]> subjectCounts = new HashMap<>(); // [total, present]
        for (Attendance a : records) {
            long[] c = subjectCounts.computeIfAbsent(a.getSubject(), k -> new long[2]);
            c[0]++;
            if (a.isPresent()) c[1]++;
        }

        // Build subject rows for all predefined subjects (+ any extras in DB)
        List<String> allSubjects = attendanceService.getSubjects();
        List<StudentSubjectAttendance> subjectRows = new ArrayList<>();
        long totalClassesAll = 0;
        long totalPresentAll = 0;

        for (String subject : allSubjects) {
            long[] c = subjectCounts.getOrDefault(subject, new long[2]);
            double pct = c[0] == 0 ? 0.0 : Math.round((c[1] * 1000.0) / c[0]) / 10.0;
            boolean shortage = c[0] > 0 && pct < SHORTAGE_THRESHOLD;
            subjectRows.add(new StudentSubjectAttendance(subject, c[0], c[1], pct, shortage));
            totalClassesAll += c[0];
            totalPresentAll += c[1];
        }

        double overallPct = totalClassesAll == 0 ? 0.0
                : Math.round((totalPresentAll * 1000.0) / totalClassesAll) / 10.0;

        return ResponseEntity.ok(new StudentDashboardResponse(
                student.getRollNo(), student.getName(), student.getClassName(),
                subjectRows, overallPct, totalClassesAll, totalPresentAll));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /**
     * Retrieves the authenticated student's roll number from the session.
     * Throws 401 if the session is not a valid student session.
     */
    private String getStudentRollNo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Object rollNo = session.getAttribute(AuthController.STUDENT_AUTH_KEY);
        if (rollNo == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return rollNo.toString();
    }
}
