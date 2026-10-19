package com.example.studentmanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

/**
 * One row = one student's attendance for one subject on one date.
 * A student can have only one record per subject per date.
 */
@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "attendance_date", "subject"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private String subject;

    @Column(name = "is_present", nullable = false)
    private boolean present;

    public Attendance() {
    }

    public Attendance(Student student, LocalDate attendanceDate, String subject, boolean present) {
        this.student = student;
        this.attendanceDate = attendanceDate;
        this.subject = subject;
        this.present = present;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
}
