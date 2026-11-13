package com.sms.controller;

import com.sms.dto.AppDTO;
import com.sms.entity.User;
import com.sms.repository.CourseRepository;
import com.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        long students = userRepository.findByRole(User.Role.STUDENT).size();
        long faculty = userRepository.findByRole(User.Role.FACULTY).size();
        long courses = courseRepository.count();

        return ResponseEntity.ok(Map.of(
                "totalStudents", students,
                "totalFaculty", faculty,
                "totalCourses", courses
        ));
    }

    @GetMapping("/students")
    public ResponseEntity<List<AppDTO.UserResponse>> getAllStudents() {
        return ResponseEntity.ok(
                userRepository.findByRole(User.Role.STUDENT)
                        .stream().map(AppDTO.UserResponse::from)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/faculty")
    public ResponseEntity<List<AppDTO.UserResponse>> getAllFaculty() {
        return ResponseEntity.ok(
                userRepository.findByRole(User.Role.FACULTY)
                        .stream().map(AppDTO.UserResponse::from)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(AppDTO.UserResponse.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody AppDTO.UserResponse req) {
        return userRepository.findById(id).map(user -> {
            if (req.getName() != null) user.setName(req.getName());
            if (req.getBranch() != null) user.setBranch(req.getBranch());
            if (req.getSemester() != null) user.setSemester(req.getSemester());
            userRepository.save(user);
            return ResponseEntity.ok(AppDTO.UserResponse.from(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(new AppDTO.MessageResponse("Student deleted"));
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> enrollStudent(@RequestBody AppDTO.EnrollRequest req) {
        User student = userRepository.findById(req.getStudentId()).orElseThrow();
        var course = courseRepository.findById(req.getCourseId()).orElseThrow();
        student.getCourses().add(course);
        userRepository.save(student);
        return ResponseEntity.ok(new AppDTO.MessageResponse("Enrolled successfully"));
    }
}
