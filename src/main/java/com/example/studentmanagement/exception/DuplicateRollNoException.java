package com.example.studentmanagement.exception;

public class DuplicateRollNoException extends RuntimeException {
    public DuplicateRollNoException(String rollNo) {
        super("Roll number already exists: " + rollNo);
    }
}
