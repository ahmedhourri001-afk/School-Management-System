# 🎓 Student Management System

[![Build Status](https://github.com/malubhai13/-Student-Management-System-/actions/workflows/ci.yml/badge.svg)](https://github.com/malubhai13/-Student-Management-System-/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2.0-61dafb.svg)](https://react.dev)
[![Vite](https://img.shields.io/badge/Vite-5.0-646cff.svg)](https://vitejs.dev)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-3.3-38b2ac.svg)](https://tailwindcss.com)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://www.mysql.com/)

A modern, production-ready full-stack web application designed for academic institutions. Built with **React**, **Spring Boot 3**, **MySQL 8**, and **JWT authentication** featuring comprehensive Role-Based Access Control (RBAC).

---

## 🌟 Features

- 🔐 **JWT Authentication & RBAC**: Endpoint and UI-level access control for **Admin**, **Faculty**, and **Student** roles.
- 👨‍💼 **Admin Portal**: Manage student rosters, faculty appointments, course offerings, and student course enrollments.
- 👩‍🏫 **Faculty Portal**: Record attendance, upload examination grades, and manage assigned curriculum courses.
- 🧑‍🎓 **Student Portal**: Real-time view of enrolled courses, attendance summaries, semester GPA/grades, and progress.
- ⚡ **Real-time WebSockets**: Instant STOMP over SockJS notifications for grade uploads and announcements.
- 📖 **Interactive API Documentation**: Embedded Swagger UI / OpenAPI 3 specification.
- 🐳 **Full Containerization**: One-command local and production deployment via Docker Compose.

---

## 🛠️ Tech Stack

| Layer | Technologies |
|:---|:---|
| **Frontend** | React 18, React Router v6, Axios, TailwindCSS, Lucide Icons, Recharts |
| **Backend** | Spring Boot 3.2, Spring Security 6, Spring Data JPA, Hibernate |
| **Authentication** | Stateless JWT (JSON Web Tokens) with BCrypt password hashing |
| **Database** | MySQL 8.0 with HikariCP connection pooling |
| **Real-time** | Spring WebSocket, STOMP protocol, SockJS client |
| **Build & Tooling** | Maven Wrapper (`mvnw`), Vite 5, PostCSS, Docker & Docker Compose |

---

## 📂 Project Structure

```
student-management-system/
├── .github/
│   ├── workflows/ci.yml       # Automated CI/CD pipeline (backend & frontend)
│   └── ISSUE_TEMPLATE/        # Standardized GitHub issue templates
├── backend/                   # Spring Boot 3 application
│   ├── .mvn/wrapper/          # Maven wrapper configuration
│   ├── mvnw / mvnw.cmd        # Cross-platform Maven wrapper scripts
│   ├── pom.xml                # Maven dependencies & build plugins
│   ├── Dockerfile             # Multi-stage container build
│   └── src/main/java/com/sms/
│       ├── config/            # Security, WebSocket, CORS & seed data
│       ├── controller/        # REST controllers
│       ├── dto/               # Strongly-typed request/response DTOs
│       ├── entity/            # JPA entities (User, Course, Attendance, Marks)
│       ├── repository/        # Spring Data JPA repositories
│       ├── security/          # JWT filter, provider & UserDetailsService
│       └── service/           # Business logic layer
├── frontend/                  # React + Vite application
│   ├── src/
│   │   ├── components/        # Reusable UI & common layout components
│   │   ├── context/           # AuthContext & state providers
│   │   ├── hooks/             # Custom hooks (WebSocket STOMP listener)
│   │   ├── pages/             # Admin, Faculty, Student & Login views
│   │   └── services/          # Axios HTTP client with auth interceptor
│   ├── Dockerfile             # Nginx-based multi-stage container
│   ├── nginx.conf             # Production reverse proxy config
│   └── vite.config.js         # Dev server & reverse proxy configuration
├── docker-compose.yml         # Multi-container orchestration (MySQL + App)
├── CONTRIBUTING.md            # Contribution & development guide
└── README.md                  # Project overview & documentation
```

---

## 🚀 Quick Start

### Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: v18 or higher (with npm)
- **MySQL**: 8.0+ (or use Docker)

---

### Option 1: Local Development

#### 1. Clone the Repository
```bash
git clone https://github.com/malubhai13/-Student-Management-System-.git
cd -Student-Management-System-
```

#### 2. Configure Database
Ensure MySQL is running, then create the database:
```sql
CREATE DATABASE sms_db;
```

Update your database credentials in `backend/src/main/resources/application.properties` or set environment variables:
```bash
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=your_password
```

#### 3. Run Backend
Using the included Maven wrapper:

**Linux / macOS:**
```bash
cd backend
./mvnw spring-boot:run
```

**Windows (PowerShell / Command Prompt):**
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
> The backend server starts at **`http://localhost:8080`**. Database tables and default accounts are seeded automatically on first boot.

#### 4. Run Frontend
```bash
cd ../frontend
npm install
npm run dev
```
> The frontend application starts at **`http://localhost:5173`**.

---

### Option 2: Docker Compose (Recommended)

Run the entire full-stack application and MySQL with a single command:

```bash
docker-compose up --build
```

- **Frontend**: `http://localhost:3000`
- **Backend API**: `http://localhost:8080`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`

To stop all containers:
```bash
docker-compose down
```

---

## 🔑 Default Login Credentials

Initial accounts are automatically populated on startup by `DataInitializer`:

| Role | Email | Password | Access Capabilities |
|:---|:---|:---|:---|
| **Admin** | `admin@sms.com` | `$SEED_ADMIN_PASSWORD` | System overview, Student & Faculty directory, Course catalog |
| **Faculty** | `faculty@sms.com` | `$SEED_FACULTY_PASSWORD` | Take attendance, Input student grades, View teaching load |
| **Student** | `student@sms.com` | `$SEED_STUDENT_PASSWORD` | View enrolled courses, Track attendance %, View semester grades |

---

## 📡 REST API Reference

Swagger UI is accessible at: **`http://localhost:8080/swagger-ui.html`**

| Method | Endpoint | Description | Role Required |
|:---|:---|:---|:---|
| `POST` | `/api/auth/login` | Authenticate user & issue JWT | Public |
| `POST` | `/api/auth/register` | Register new user account | Admin |
| `GET` | `/api/admin/dashboard` | Aggregated institutional metrics | Admin |
| `GET` | `/api/admin/students` | Retrieve all student profiles | Admin |
| `POST` | `/api/admin/enroll` | Enroll a student into a course | Admin |
| `DELETE` | `/api/admin/students/{id}` | Delete a student profile | Admin |
| `GET` | `/api/courses` | List all academic courses | Authenticated |
| `POST` | `/api/courses` | Create a new course entry | Admin |
| `POST` | `/api/attendance` | Record course attendance | Faculty |
| `GET` | `/api/attendance/student/{id}` | Fetch student attendance history | Faculty, Student |
| `POST` | `/api/marks` | Upload course examination grades | Faculty |
| `GET` | `/api/marks/student/{id}` | Fetch student grade report | Faculty, Student |
| `WS` | `/ws` | STOMP WebSocket communication | Authenticated |

---

## 🤝 Contributing

Contributions are always welcome! Please check out [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on branch naming, code style, and submitting pull requests.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
