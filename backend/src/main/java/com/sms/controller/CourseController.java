package com.sms.controller;

import com.sms.dto.AppDTO;
import com.sms.entity.*;
import com.sms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ===================== COURSES =====================

    @GetMapping("/courses")
    public ResponseEntity<List<AppDTO.CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(
                courseRepository.findAll().stream()
                        .map(AppDTO.CourseResponse::from)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<?> getCourse(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(c -> ResponseEntity.ok(AppDTO.CourseResponse.from(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCourse(@RequestBody AppDTO.CourseRequest req) {
        User faculty = req.getFacultyId() != null
                ? userRepository.findById(req.getFacultyId()).orElse(null)
                : null;

        Course course = Course.builder()
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .credits(req.getCredits())
                .semester(req.getSemester())
                .faculty(faculty)
                .build();

        return ResponseEntity.ok(AppDTO.CourseResponse.from(courseRepository.save(course)));
    }

    @DeleteMapping("/courses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        courseRepository.deleteById(id);
        return ResponseEntity.ok(new AppDTO.MessageResponse("Course deleted"));
    }

    @GetMapping("/faculty/courses")
    @PreAuthorize("hasRole('ADMIN') or @accessGuard.isSelf(authentication, #facultyId)")
    public ResponseEntity<List<AppDTO.CourseResponse>> getFacultyCourses(
            @RequestParam Long facultyId) {
        return ResponseEntity.ok(
                courseRepository.findByFacultyId(facultyId).stream()
                        .map(AppDTO.CourseResponse::from)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/faculty/courses/{courseId}/students")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<List<AppDTO.UserResponse>> getCourseStudents(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                userRepository.findByCoursesId(courseId).stream()
                        .map(AppDTO.UserResponse::from)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/student/courses")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN') or @accessGuard.isSelf(authentication, #studentId)")
    public ResponseEntity<List<AppDTO.CourseResponse>> getStudentCourses(@RequestParam Long studentId) {
        User student = userRepository.findById(studentId).orElseThrow();
        return ResponseEntity.ok(
                student.getCourses().stream()
                        .map(AppDTO.CourseResponse::from)
                        .collect(Collectors.toList())
        );
    }

    // ===================== ATTENDANCE =====================

    @PostMapping("/attendance")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<?> markAttendance(@RequestBody AppDTO.AttendanceRequest req) {
        User student = userRepository.findById(req.getStudentId()).orElseThrow();
        Course course = courseRepository.findById(req.getCourseId()).orElseThrow();

        Attendance attendance = Attendance.builder()
                .student(student)
                .course(course)
                .date(req.getDate())
                .status(req.getStatus())
                .build();

        return ResponseEntity.ok(AppDTO.AttendanceResponse.from(attendanceRepository.save(attendance)));
    }

    @PutMapping("/attendance/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<?> updateAttendance(@PathVariable Long id,
                                              @RequestBody AppDTO.AttendanceRequest req) {
        return attendanceRepository.findById(id)
                .map(attendance -> {
                    if (req.getDate() != null) attendance.setDate(req.getDate());
                    if (req.getStatus() != null) attendance.setStatus(req.getStatus());
                    return ResponseEntity.ok(AppDTO.AttendanceResponse.from(attendanceRepository.save(attendance)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/attendance/student/{studentId}")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN') or @accessGuard.isSelf(authentication, #studentId)")
    public ResponseEntity<List<AppDTO.AttendanceResponse>> getStudentAttendance(@PathVariable Long studentId) {
        List<AppDTO.AttendanceResponse> result = attendanceRepository.findByStudentId(studentId)
                .stream().map(a -> {
                    AppDTO.AttendanceResponse resp = AppDTO.AttendanceResponse.from(a);
                    Long present = attendanceRepository.countPresentByStudentAndCourse(studentId, a.getCourse().getId());
                    Long total = attendanceRepository.countTotalByStudentAndCourse(studentId, a.getCourse().getId());
                    resp.setPercentage(total > 0 ? Math.round((present * 100.0 / total) * 10.0) / 10.0 : 0);
                    return resp;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attendance/course/{courseId}")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<List<AppDTO.AttendanceResponse>> getCourseAttendance(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                attendanceRepository.findAll().stream()
                        .filter(a -> a.getCourse().getId().equals(courseId))
                        .map(AppDTO.AttendanceResponse::from)
                        .collect(Collectors.toList())
        );
    }

    // ===================== MARKS =====================

    @PostMapping("/marks")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<?> uploadMarks(@RequestBody AppDTO.MarksRequest req) {
        User student = userRepository.findById(req.getStudentId()).orElseThrow();
        Course course = courseRepository.findById(req.getCourseId()).orElseThrow();

        Marks marks = Marks.builder()
                .student(student)
                .course(course)
                .examType(Marks.ExamType.valueOf(req.getExamType()))
                .marksObtained(req.getMarksObtained())
                .totalMarks(req.getTotalMarks())
                .build();

        Marks saved = marksRepository.save(marks);

        // WebSocket notification to student
        AppDTO.Notification notification = AppDTO.Notification.builder()
                .type("GRADE_UPDATE")
                .message("New marks uploaded for " + course.getName())
                .courseName(course.getName())
                .grade(saved.getGrade())
                .build();

        messagingTemplate.convertAndSendToUser(
                student.getEmail(), "/queue/notifications", notification
        );

        return ResponseEntity.ok(AppDTO.MarksResponse.from(saved));
    }

    @PutMapping("/marks/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<?> updateMarks(@PathVariable Long id, @RequestBody AppDTO.MarksRequest req) {
        Marks marks = marksRepository.findById(id).orElse(null);
        if (marks == null) {
            return ResponseEntity.notFound().build();
        }

        // On valide d'abord avec des variables locales : rien n'est modifié si les valeurs sont invalides
        double obtained = req.getMarksObtained() != null ? req.getMarksObtained() : marks.getMarksObtained();
        double total = req.getTotalMarks() != null ? req.getTotalMarks() : marks.getTotalMarks();
        if (total <= 0 || obtained < 0 || obtained > total) {
            return ResponseEntity.badRequest().body(new AppDTO.MessageResponse("Invalid marks"));
        }

        Marks.ExamType examType = marks.getExamType();
        if (req.getExamType() != null) {
            try {
                examType = Marks.ExamType.valueOf(req.getExamType());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new AppDTO.MessageResponse("Invalid exam type"));
            }
        }

        marks.setExamType(examType);
        marks.setMarksObtained(obtained);
        marks.setTotalMarks(total);

        // saveAndFlush : la note (grade) est recalculée par @PreUpdate avant la réponse
        Marks saved = marksRepository.saveAndFlush(marks);

        AppDTO.Notification notification = AppDTO.Notification.builder()
                .type("GRADE_UPDATE")
                .message("Marks updated for " + saved.getCourse().getName())
                .courseName(saved.getCourse().getName())
                .grade(saved.getGrade())
                .build();
        messagingTemplate.convertAndSendToUser(
                saved.getStudent().getEmail(), "/queue/notifications", notification
        );

        return ResponseEntity.ok(AppDTO.MarksResponse.from(saved));
    }

    @GetMapping("/marks/student/{studentId}")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN') or @accessGuard.isSelf(authentication, #studentId)")
    public ResponseEntity<List<AppDTO.MarksResponse>> getStudentMarks(@PathVariable Long studentId) {
        return ResponseEntity.ok(
                marksRepository.findByStudentId(studentId).stream()
                        .map(AppDTO.MarksResponse::from)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/marks/course/{courseId}")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<List<AppDTO.MarksResponse>> getCourseMarks(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                marksRepository.findByCourseId(courseId).stream()
                        .map(AppDTO.MarksResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
