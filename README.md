# Student Attendance Management System

A web-based **Student Attendance Management System** built with **Spring Boot, Java, HTML, CSS, and JavaScript**. The application allows an administrator to manage students, mark daily subject-wise attendance, and monitor attendance percentages with shortage detection.

## 🚀 Live Demo

**Live Application:** https://student-attendance-system-production-de23.up.railway.app/

## ✨ Features

- Admin login and dashboard
- Student management
- Add and delete students
- Subject-wise daily attendance
- Mark students as Present / Absent
- Automatic attendance percentage calculation
- Attendance report for each student
- Shortage detection for attendance below **75%**
- Subject filtering
- REST API for students and attendance
- H2 database for lightweight development and demonstration
- Sample student and attendance data loaded automatically on startup
- Responsive web interface

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Backend programming |
| Spring Boot | Application framework |
| Spring Data JPA | Database access and ORM |
| Hibernate | JPA implementation |
| H2 Database | In-memory database |
| HTML5 | Frontend structure |
| CSS3 | Styling and responsive UI |
| JavaScript | Frontend functionality and API communication |
| Maven | Build and dependency management |
| Railway | Cloud deployment |

## 📋 Requirements

- JDK 17 or above
- Maven 3.8+
- Internet connection on the first run to download dependencies

## ▶️ Run Locally

Clone the repository:

```bash
git clone https://github.com/sohamwadekar04/student-attendance-system.git
cd student-attendance-system
```

Start the application:

```bash
mvn spring-boot:run
```

Open the application in your browser:

**http://localhost:8080**

### H2 Database Console

**http://localhost:8080/h2-console**

- JDBC URL: `jdbc:h2:mem:studentdb`
- Username: `sa`
- Password: *(empty)*

> The application uses an in-memory H2 database. Sample data is recreated when the application starts.

## 👨‍💻 Sample Data

The application includes six sample students and **20 Java attendance records per student** for demonstration purposes.

| Roll No. | Student | Attendance |
|---|---|---:|
| 101 | Nihar Vategavkar | 100% |
| 102 | Soham Wadekar | 75% |
| 103 | Sakshi Bendke | 75% |
| 104 | Sahara Patil | 70% |
| 105 | Neha Deshmukh | 25% |
| 106 | Riya Sharma | 70% |

Students below 75% are identified as having attendance shortage.

## 🔌 REST API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/students` | List all students |
| POST | `/api/students` | Add a student |
| DELETE | `/api/students/{id}` | Delete a student and their attendance |
| POST | `/api/attendance` | Save attendance for multiple students |
| GET | `/api/attendance?date=&subject=` | Get attendance for a date and subject |
| GET | `/api/attendance/report?subject=` | Get attendance percentage report |
| GET | `/api/attendance/subjects` | Get available subjects |

### Example Attendance Request

```json
{
  "date": "2026-10-18",
  "subject": "Java",
  "records": [
    {"studentId": 1, "present": true},
    {"studentId": 2, "present": false}
  ]
}
```

## 📁 Project Structure

```text
src/main/java/com/example/studentmanagement
├── model/        Student, Attendance
├── dto/          AttendanceRequest, AttendanceResponse, ReportRow
├── repository/   StudentRepository, AttendanceRepository
├── service/      StudentService, AttendanceService
├── controller/   StudentController, AttendanceController
├── exception/    Custom exceptions and GlobalExceptionHandler
└── config/       DataLoader

src/main/resources
├── application.properties
└── static/
    ├── index.html
    ├── style.css
    └── app.js
```

## ☁️ Deployment

The application is deployed on **Railway** using the GitHub repository:

`sohamwadekar04/student-attendance-system`

Every fresh deployment starts the Spring Boot application and loads the configured sample data.

## 🔮 Future Scope

- Student login with personal attendance view
- PostgreSQL/MySQL database for persistent production data
- Authentication and role-based access control
- Attendance analytics and charts
- Export attendance reports
- Email or notification alerts for attendance shortage

## 📄 License

This project is developed as an academic/college project for learning and demonstration purposes.
