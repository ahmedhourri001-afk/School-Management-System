package com.sms.controller;

import com.sms.entity.Attendance;
import com.sms.entity.Marks;
import com.sms.repository.AttendanceRepository;
import com.sms.repository.MarksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Export CSV des absences et des notes, réservé à l'administration. */
@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ExportController {

    private static final String BOM = "\uFEFF"; // permet à Excel de lire correctement les accents
    private static final String EOL = "\r\n";

    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;

    @GetMapping("/attendance")
    public ResponseEntity<String> exportAttendance() {
        StringBuilder csv = new StringBuilder(BOM);
        csv.append("id,student_name,student_email,roll_number,course_code,course_name,date,status").append(EOL);

        for (Attendance a : attendanceRepository.findAll(Sort.by("id"))) {
            csv.append(row(
                    String.valueOf(a.getId()),
                    a.getStudent().getName(),
                    a.getStudent().getEmail(),
                    a.getStudent().getRollNumber(),
                    a.getCourse().getCode(),
                    a.getCourse().getName(),
                    String.valueOf(a.getDate()),
                    a.getStatus().name()
            ));
        }
        return csvResponse(csv.toString(), "absences.csv");
    }

    @GetMapping("/marks")
    public ResponseEntity<String> exportMarks() {
        StringBuilder csv = new StringBuilder(BOM);
        csv.append("id,student_name,student_email,roll_number,course_code,course_name,exam_type,marks_obtained,total_marks,grade").append(EOL);

        for (Marks m : marksRepository.findAll(Sort.by("id"))) {
            csv.append(row(
                    String.valueOf(m.getId()),
                    m.getStudent().getName(),
                    m.getStudent().getEmail(),
                    m.getStudent().getRollNumber(),
                    m.getCourse().getCode(),
                    m.getCourse().getName(),
                    m.getExamType().name(),
                    String.valueOf(m.getMarksObtained()),
                    String.valueOf(m.getTotalMarks()),
                    m.getGrade()
            ));
        }
        return csvResponse(csv.toString(), "notes.csv");
    }

    private ResponseEntity<String> csvResponse(String body, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    private String row(String... cells) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) line.append(',');
            line.append(cell(cells[i]));
        }
        return line.append(EOL).toString();
    }

    /** Échappe une cellule CSV et neutralise l'injection de formules Excel (=, +, -, @ en début de cellule). */
    private String cell(String value) {
        if (value == null) return "";
        String v = value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
