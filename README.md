# Student Attendance Management System (Spring Boot + HTML/CSS/JS)

Add students, mark subject-wise daily attendance, and view an attendance
percentage report that flags students below 75%.

## Requirements
- JDK 17 or above
- Maven 3.8+ (or the Maven built into IntelliJ / Eclipse / VS Code)
- Internet connection on first run (to download dependencies)

## Run
    mvn spring-boot:run

Open http://localhost:8080

H2 console: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:studentdb   User: sa   Password: (empty)

Sample students and 4 days of "Java" attendance are loaded on every start.

## REST API
| Method | URL                                   | Description                                   |
|--------|---------------------------------------|-----------------------------------------------|
| GET    | /api/students                         | List all students                             |
| POST   | /api/students                         | Add a student                                 |
| DELETE | /api/students/{id}                    | Delete a student (and their attendance)       |
| POST   | /api/attendance                       | Save attendance for many students at once     |
| GET    | /api/attendance?date=&subject=        | Get saved attendance for a date and subject   |
| GET    | /api/attendance/report?subject=       | Attendance % per student (subject optional)   |
| GET    | /api/attendance/subjects              | Distinct subjects                             |

Example body for POST /api/attendance:
    {"date":"2026-10-18","subject":"Java","records":[{"studentId":1,"present":true},{"studentId":2,"present":false}]}

## Structure
    src/main/java/com/example/studentmanagement
      model/       Student, Attendance (JPA entities)
      dto/         AttendanceRequest, AttendanceResponse, ReportRow
      repository/  StudentRepository, AttendanceRepository (Spring Data JPA)
      service/     StudentService, AttendanceService (business logic)
      controller/  StudentController, AttendanceController (REST)
      exception/   custom exceptions + GlobalExceptionHandler
      config/      DataLoader (sample data)
    src/main/resources
      application.properties
      static/      index.html, style.css, app.js
