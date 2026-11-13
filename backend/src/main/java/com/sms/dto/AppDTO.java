package com.sms.dto;

import com.sms.entity.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

public class AppDTO {

    // ---- User ----
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String rollNumber;
        private String branch;
        private Integer semester;
        private String department;
        private String designation;

        public static UserResponse from(User u) {
            return UserResponse.builder()
                    .id(u.getId())
                    .name(u.getName())
                    .email(u.getEmail())
                    .role(u.getRole().name())
                    .rollNumber(u.getRollNumber())
                    .branch(u.getBranch())
                    .semester(u.getSemester())
                    .department(u.getDepartment())
                    .designation(u.getDesignation())
                    .build();
        }
    }

    // ---- Course ----
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CourseResponse {
        private Long id;
        private String code;
        private String name;
        private String description;
        private Integer credits;
        private Integer semester;
        private String facultyName;
        private Long facultyId;
        private Integer studentCount;

        public static CourseResponse from(Course c) {
            return CourseResponse.builder()
                    .id(c.getId())
                    .code(c.getCode())
                    .name(c.getName())
                    .description(c.getDescription())
                    .credits(c.getCredits())
                    .semester(c.getSemester())
                    .facultyName(c.getFaculty() != null ? c.getFaculty().getName() : null)
                    .facultyId(c.getFaculty() != null ? c.getFaculty().getId() : null)
                    .studentCount(c.getStudents() != null ? c.getStudents().size() : 0)
                    .build();
        }
    }

    @Getter @Setter
    public static class CourseRequest {
        private String code;
        private String name;
        private String description;
        private Integer credits;
        private Integer semester;
        private Long facultyId;
    }

    // ---- Attendance ----
    @Getter @Setter
    public static class AttendanceRequest {
        private Long studentId;
        private Long courseId;
        private LocalDate date;
        private Attendance.Status status;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AttendanceResponse {
        private Long id;
        private String studentName;
        private String courseName;
        private LocalDate date;
        private String status;
        private Double percentage;

        public static AttendanceResponse from(Attendance a) {
            return AttendanceResponse.builder()
                    .id(a.getId())
                    .studentName(a.getStudent().getName())
                    .courseName(a.getCourse().getName())
                    .date(a.getDate())
                    .status(a.getStatus().name())
                    .build();
        }
    }

    // ---- Marks ----
    @Getter @Setter
    public static class MarksRequest {
        private Long studentId;
        private Long courseId;
        private String examType;
        private Double marksObtained;
        private Double totalMarks;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MarksResponse {
        private Long id;
        private String studentName;
        private String courseName;
        private String examType;
        private Double marksObtained;
        private Double totalMarks;
        private String grade;
        private Double percentage;

        public static MarksResponse from(Marks m) {
            double pct = (m.getMarksObtained() / m.getTotalMarks()) * 100;
            return MarksResponse.builder()
                    .id(m.getId())
                    .studentName(m.getStudent().getName())
                    .courseName(m.getCourse().getName())
                    .examType(m.getExamType().name())
                    .marksObtained(m.getMarksObtained())
                    .totalMarks(m.getTotalMarks())
                    .grade(m.getGrade())
                    .percentage(Math.round(pct * 10.0) / 10.0)
                    .build();
        }
    }

    // ---- Generic ----
    @Getter @Setter @AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }

    @Getter @Setter @AllArgsConstructor
    public static class EnrollRequest {
        private Long studentId;
        private Long courseId;
    }

    // ---- Notification ----
    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Notification {
        private String type;
        private String message;
        private String courseName;
        private String grade;
    }
}
