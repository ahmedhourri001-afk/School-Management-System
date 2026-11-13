package com.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "marks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Marks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamType examType;

    @Column(nullable = false)
    private Double marksObtained;

    @Column(nullable = false)
    private Double totalMarks;

    private String grade;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        uploadedAt = LocalDateTime.now();
        // Auto-calculate grade
        double percentage = (marksObtained / totalMarks) * 100;
        if (percentage >= 90) grade = "A+";
        else if (percentage >= 80) grade = "A";
        else if (percentage >= 70) grade = "B";
        else if (percentage >= 60) grade = "C";
        else if (percentage >= 50) grade = "D";
        else grade = "F";
    }

    public enum ExamType {
        MIDTERM, FINAL, ASSIGNMENT, QUIZ
    }
}
