package com.sms.controller;

import com.sms.dto.AppDTO;
import com.sms.entity.Attendance;
import com.sms.entity.Course;
import com.sms.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seuil maximal d'absences. La valeur vient de la configuration externe (ABSENCE_MAX_THRESHOLD),
 * donc de la ConfigMap Kubernetes : la changer ne demande aucun rebuild de l'image.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AbsenceController {

    private final AttendanceRepository attendanceRepository;

    @Value("${app.absence.max-threshold}")
    private int maxAbsences;

    @GetMapping("/config/absence-threshold")
    public ResponseEntity<Map<String, Integer>> getAbsenceThreshold() {
        return ResponseEntity.ok(Map.of("maxAbsences", maxAbsences));
    }

    /** Pour chaque cours : nombre d'absences (statut ABSENT) et dépassement du seuil (absences > seuil). */
    @GetMapping("/attendance/student/{studentId}/summary")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN') or @accessGuard.isSelf(authentication, #studentId)")
    public ResponseEntity<List<AppDTO.AbsenceSummary>> getAbsenceSummary(@PathVariable Long studentId) {
        Map<Long, AppDTO.AbsenceSummary> byCourse = new LinkedHashMap<>();

        for (Attendance record : attendanceRepository.findByStudentId(studentId)) {
            Course course = record.getCourse();
            AppDTO.AbsenceSummary summary = byCourse.computeIfAbsent(course.getId(), id ->
                    AppDTO.AbsenceSummary.builder()
                            .courseId(id)
                            .courseName(course.getName())
                            .absences(0)
                            .maxAbsences(maxAbsences)
                            .exceeded(false)
                            .build());
            if (record.getStatus() == Attendance.Status.ABSENT) {
                summary.setAbsences(summary.getAbsences() + 1);
            }
        }

        byCourse.values().forEach(s -> s.setExceeded(s.getAbsences() > maxAbsences));
        return ResponseEntity.ok(new ArrayList<>(byCourse.values()));
    }
}
