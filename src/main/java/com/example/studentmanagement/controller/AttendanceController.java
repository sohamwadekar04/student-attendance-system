package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.AttendanceRequest;
import com.example.studentmanagement.dto.AttendanceResponse;
import com.example.studentmanagement.dto.ReportRow;
import com.example.studentmanagement.service.AttendanceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @PostMapping
    public List<AttendanceResponse> mark(@Valid @RequestBody AttendanceRequest request) {
        return service.mark(request);
    }

    @GetMapping
    public List<AttendanceResponse> getByDateAndSubject(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String subject) {
        return service.getByDateAndSubject(date, subject);
    }

    @GetMapping("/report")
    public List<ReportRow> report(@RequestParam(required = false) String subject) {
        return service.report(subject);
    }

    @GetMapping("/subjects")
    public List<String> subjects() {
        return service.getSubjects();
    }
}
