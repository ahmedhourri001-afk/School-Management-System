package com.sms.config;

import com.sms.entity.Course;
import com.sms.entity.User;
import com.sms.repository.CourseRepository;
import com.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Value("${app.seed.faculty-password}")
    private String facultyPassword;

    @Value("${app.seed.student-password}")
    private String studentPassword;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        log.info("Seeding initial data...");

        // Admin
        User admin = User.builder()
                .name("System Admin")
                .email("admin@sms.com")
                .password(passwordEncoder.encode(adminPassword))
                .role(User.Role.ADMIN)
                .build();
        userRepository.save(admin);

        // Faculty
        User faculty = User.builder()
                .name("Dr. Priya Sharma")
                .email("faculty@sms.com")
                .password(passwordEncoder.encode(facultyPassword))
                .role(User.Role.FACULTY)
                .department("Computer Science")
                .designation("Associate Professor")
                .build();
        userRepository.save(faculty);

        // Student
        User student = User.builder()
                .name("Rahul Gupta")
                .email("student@sms.com")
                .password(passwordEncoder.encode(studentPassword))
                .role(User.Role.STUDENT)
                .rollNumber("CSE2023001")
                .branch("Computer Science")
                .semester(4)
                .build();
        userRepository.save(student);

        // Courses
        Course dsa = Course.builder()
                .code("CS301")
                .name("Data Structures & Algorithms")
                .description("Fundamental data structures and algorithm design")
                .credits(4)
                .semester(3)
                .faculty(faculty)
                .build();
        courseRepository.save(dsa);

        Course dbms = Course.builder()
                .code("CS302")
                .name("Database Management Systems")
                .description("Relational databases, SQL, and transactions")
                .credits(4)
                .semester(4)
                .faculty(faculty)
                .build();
        courseRepository.save(dbms);

        Course os = Course.builder()
                .code("CS303")
                .name("Operating Systems")
                .description("Process management, memory, file systems")
                .credits(3)
                .semester(4)
                .faculty(faculty)
                .build();
        courseRepository.save(os);

        // Enroll student
        student.getCourses().add(dsa);
        student.getCourses().add(dbms);
        student.getCourses().add(os);
        userRepository.save(student);

        log.info("Data seeded successfully!");
    }
}
