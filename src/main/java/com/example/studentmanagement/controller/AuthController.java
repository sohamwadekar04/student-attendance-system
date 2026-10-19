package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.LoginRequest;
import com.example.studentmanagement.dto.LoginResponse;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String AUTH_USER_KEY = "authenticatedUser";
    public static final String STUDENT_AUTH_KEY = "authenticatedStudent";
    public static final String DEMO_USERNAME = "admin";
    public static final String DEMO_PASSWORD = "admin123";

    private final StudentRepository studentRepository;

    public AuthController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String password = request.getPassword() != null ? request.getPassword() : "";

        if (DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password)) {
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(AUTH_USER_KEY, DEMO_USERNAME);
            return ResponseEntity.ok(new LoginResponse(true, "Login successful"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(false, "Invalid username or password"));
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(new LoginResponse(true, "Logged out successfully"));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        boolean authenticated = session != null && session.getAttribute(AUTH_USER_KEY) != null;
        return ResponseEntity.ok(Map.of("authenticated", authenticated));
    }

    // ── Student auth ──────────────────────────────────────────────────────────

    /**
     * Student login: username = roll number, password = roll number (default).
     */
    @PostMapping("/student-login")
    public ResponseEntity<LoginResponse> studentLogin(@Valid @RequestBody LoginRequest request,
                                                      HttpServletRequest httpRequest) {
        String rollNo = request.getUsername() != null ? request.getUsername().trim() : "";
        String password = request.getPassword() != null ? request.getPassword() : "";

        // Default password equals the student's roll number
        Student student = studentRepository.findByRollNoIgnoreCase(rollNo).orElse(null);
        if (student != null && student.getRollNo().equals(password)) {
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(STUDENT_AUTH_KEY, student.getRollNo());
            return ResponseEntity.ok(new LoginResponse(true, "Login successful"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(false, "Invalid roll number or password"));
    }

    @PostMapping("/student-logout")
    public ResponseEntity<LoginResponse> studentLogout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(new LoginResponse(true, "Logged out successfully"));
    }

    @GetMapping("/student-status")
    public ResponseEntity<Map<String, Object>> studentStatus(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        boolean authenticated = session != null && session.getAttribute(STUDENT_AUTH_KEY) != null;
        return ResponseEntity.ok(Map.of("authenticated", authenticated));
    }
}
