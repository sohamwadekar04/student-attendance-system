package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.AttendanceRequest;
import com.example.studentmanagement.dto.AttendanceResponse;
import com.example.studentmanagement.dto.ReportRow;
import com.example.studentmanagement.exception.StudentNotFoundException;
import com.example.studentmanagement.model.Attendance;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.AttendanceRepository;
import com.example.studentmanagement.repository.StudentRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    /** Canonical list of subjects supported by the system. */
    private static final List<String> PREDEFINED_SUBJECTS = List.of(
            "Java", "COA", "AOA", "MCE", "DSJT", "IBS", "ED", "ENVS");

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    /** Saves attendance for many students at once. Existing records for the same date and subject are updated. */
    @Transactional
    public List<AttendanceResponse> mark(AttendanceRequest request) {
        String subject = request.subject().trim();
        List<AttendanceResponse> saved = new ArrayList<>();

        for (AttendanceRequest.Entry entry : request.records()) {
            Student student = studentRepository.findById(entry.studentId())
                    .orElseThrow(() -> new StudentNotFoundException(entry.studentId()));

            Attendance record = attendanceRepository
                    .findByStudentIdAndAttendanceDateAndSubject(student.getId(), request.date(), subject)
                    .orElseGet(() -> new Attendance(student, request.date(), subject, false));

            record.setPresent(entry.present());
            saved.add(toResponse(attendanceRepository.save(record)));
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getByDateAndSubject(LocalDate date, String subject) {
        return attendanceRepository.findByAttendanceDateAndSubject(date, subject.trim())
                .stream().map(this::toResponse).toList();
    }

    /**
     * Returns all predefined subjects first (in order), then any additional
     * subjects that exist only in the database (e.g. from older records).
     */
    public List<String> getSubjects() {
        // Start with the predefined canonical list
        Set<String> seen = new LinkedHashSet<>(PREDEFINED_SUBJECTS);
        // Append any extra subjects from existing attendance records
        attendanceRepository.findDistinctSubjects().forEach(seen::add);
        return new ArrayList<>(seen);
    }

    /** Attendance percentage per student, optionally for one subject. */
    @Transactional(readOnly = true)
    public List<ReportRow> report(String subject) {
        List<Attendance> records = (subject == null || subject.isBlank())
                ? attendanceRepository.findAll()
                : attendanceRepository.findBySubject(subject.trim());

        Map<Long, long[]> counts = new HashMap<>(); // [total, present]
        for (Attendance a : records) {
            long[] c = counts.computeIfAbsent(a.getStudent().getId(), k -> new long[2]);
            c[0]++;
            if (a.isPresent()) {
                c[1]++;
            }
        }

        List<ReportRow> rows = new ArrayList<>();
        for (Student s : studentRepository.findAllByOrderByRollNoAsc()) {
            long[] c = counts.getOrDefault(s.getId(), new long[2]);
            double pct = c[0] == 0 ? 0.0 : Math.round((c[1] * 1000.0) / c[0]) / 10.0;
            rows.add(new ReportRow(s.getId(), s.getRollNo(), s.getName(), c[0], c[1], pct));
        }
        return rows;
    }

    private AttendanceResponse toResponse(Attendance a) {
        Student s = a.getStudent();
        return new AttendanceResponse(s.getId(), s.getRollNo(), s.getName(),
                a.getAttendanceDate(), a.getSubject(), a.isPresent());
    }
}
