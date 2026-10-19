package com.example.studentmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record AttendanceRequest(
        @NotNull(message = "Date is required") LocalDate date,
        @NotBlank(message = "Subject is required") String subject,
        @NotEmpty(message = "At least one student record is required") @Valid List<Entry> records) {

    public record Entry(
            @NotNull(message = "Student id is required") Long studentId,
            @NotNull(message = "Present flag is required") Boolean present) {
    }
}
