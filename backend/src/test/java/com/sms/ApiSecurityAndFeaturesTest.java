package com.sms;

import com.sms.entity.Attendance;
import com.sms.entity.Course;
import com.sms.entity.Marks;
import com.sms.entity.User;
import com.sms.repository.AttendanceRepository;
import com.sms.repository.CourseRepository;
import com.sms.repository.MarksRepository;
import com.sms.repository.UserRepository;
import com.sms.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de l'API : contrôle d'accès, modification, export, seuil d'absences, santé.
 * Chaque test est exécuté dans une transaction annulée à la fin (@Transactional) : les tests sont indépendants.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiSecurityAndFeaturesTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired UserDetailsService userDetailsService;
    @Autowired UserRepository userRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired AttendanceRepository attendanceRepository;
    @Autowired MarksRepository marksRepository;

    private User student;      // student@sms.com (créé par DataInitializer)
    private User otherStudent; // un second étudiant, créé pour les tests
    private Course course;
    private Attendance attendance;
    private Marks marks;

    @BeforeEach
    void setUp() {
        student = userRepository.findByEmail("student@sms.com").orElseThrow();
        otherStudent = userRepository.save(User.builder()
                .name("Autre Etudiant").email("autre@sms.com").password("non-utilise")
                .role(User.Role.STUDENT).build());
        course = courseRepository.save(Course.builder().code("TEST-101").name("Cours de test").build());

        // 2 absences pour "student" (seuil de test = 1, donc seuil dépassé)
        attendance = attendanceRepository.save(Attendance.builder()
                .student(student).course(course).date(LocalDate.now()).status(Attendance.Status.ABSENT).build());
        attendanceRepository.save(Attendance.builder()
                .student(student).course(course).date(LocalDate.now().minusDays(1)).status(Attendance.Status.ABSENT).build());

        marks = marksRepository.save(Marks.builder()
                .student(student).course(course).examType(Marks.ExamType.MIDTERM)
                .marksObtained(50.0).totalMarks(100.0).build());

        student.getCourses().add(course);
        userRepository.save(student);
    }

    private String bearer(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return "Bearer " + jwtUtil.generateToken(userDetails, role);
    }

    // ---------- Contrôle d'accès : un étudiant ne voit que son propre dossier ----------

    @Test
    void studentCanReadOwnAttendanceAndMarks() throws Exception {
        mockMvc.perform(get("/api/attendance/student/" + student.getId())
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/marks/student/" + student.getId())
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isOk());
    }

    @Test
    void studentCannotReadAnotherStudentsAttendance() throws Exception {
        mockMvc.perform(get("/api/attendance/student/" + otherStudent.getId())
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotReadAnotherStudentsMarks() throws Exception {
        mockMvc.perform(get("/api/marks/student/" + otherStudent.getId())
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotReadAnotherStudentsCoursesOrSummary() throws Exception {
        mockMvc.perform(get("/api/student/courses").param("studentId", String.valueOf(otherStudent.getId()))
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/attendance/student/" + otherStudent.getId() + "/summary")
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void facultyCanReadAnyStudentRecord() throws Exception {
        mockMvc.perform(get("/api/attendance/student/" + student.getId())
                        .header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/marks/student/" + student.getId())
                        .header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/attendance/student/" + student.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void facultyCanOnlyListOwnCourses() throws Exception {
        Long facultyId = userRepository.findByEmail("faculty@sms.com").orElseThrow().getId();
        mockMvc.perform(get("/api/faculty/courses").param("facultyId", String.valueOf(facultyId))
                        .header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/faculty/courses").param("facultyId", String.valueOf(otherStudent.getId()))
                        .header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isForbidden());
    }

    // ---------- Inscription : réservée à l'administration ----------

    private static final String NEW_USER_JSON =
            "{\"name\":\"Nouveau\",\"email\":\"nouveau@sms.com\",\"password\":\"mot-de-passe-test\",\"role\":\"STUDENT\"}";

    @Test
    void registerIsRejectedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(NEW_USER_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void registerIsForbiddenForStudent() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(NEW_USER_JSON)
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerIsAllowedForAdmin() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(NEW_USER_JSON)
                        .header("Authorization", bearer("admin@sms.com")))
                .andExpect(status().isOk());
    }

    // ---------- Modification des absences et des notes ----------

    @Test
    void facultyCanUpdateAttendance() throws Exception {
        mockMvc.perform(put("/api/attendance/" + attendance.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PRESENT\"}")
                        .header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRESENT"));
    }

    @Test
    void studentCannotUpdateAttendanceOrMarks() throws Exception {
        mockMvc.perform(put("/api/attendance/" + attendance.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PRESENT\"}")
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/marks/" + marks.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"marksObtained\":100}")
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatingMarksRecalculatesGrade() throws Exception {
        mockMvc.perform(put("/api/marks/" + marks.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"marksObtained\":95,\"totalMarks\":100}")
                        .header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("A+"));
    }

    @Test
    void updatingMarksRejectsInvalidValues() throws Exception {
        mockMvc.perform(put("/api/marks/" + marks.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"marksObtained\":150,\"totalMarks\":100}")
                        .header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatingUnknownRecordReturns404() throws Exception {
        mockMvc.perform(put("/api/attendance/999999")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PRESENT\"}")
                        .header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void facultyCanListStudentsOfACourse() throws Exception {
        mockMvc.perform(get("/api/faculty/courses/" + course.getId() + "/students")
                        .header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(hasItem("student@sms.com")));
        mockMvc.perform(get("/api/faculty/courses/" + course.getId() + "/students")
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isForbidden());
    }

    // ---------- Export CSV (administration uniquement) ----------

    @Test
    void adminCanExportAttendanceAndMarksAsCsv() throws Exception {
        mockMvc.perform(get("/api/admin/export/attendance").header("Authorization", bearer("admin@sms.com")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("TEST-101")));
        mockMvc.perform(get("/api/admin/export/marks").header("Authorization", bearer("admin@sms.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("TEST-101")));
    }

    @Test
    void nonAdminCannotExport() throws Exception {
        mockMvc.perform(get("/api/admin/export/attendance").header("Authorization", bearer("faculty@sms.com")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/export/marks").header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isForbidden());
    }

    // ---------- Seuil d'absences (valeur de test : 1) ----------

    @Test
    void thresholdComesFromConfiguration() throws Exception {
        mockMvc.perform(get("/api/config/absence-threshold").header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxAbsences").value(1));
    }

    @Test
    void absenceSummaryFlagsExceededThreshold() throws Exception {
        mockMvc.perform(get("/api/attendance/student/" + student.getId() + "/summary")
                        .header("Authorization", bearer("student@sms.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.courseName=='Cours de test')].absences").value(hasItem(2)))
                .andExpect(jsonPath("$[?(@.courseName=='Cours de test')].exceeded").value(hasItem(true)));
    }

    // ---------- Santé de l'application (Kubernetes + Nagios) ----------

    @Test
    void healthEndpointIsPublicAndUp() throws Exception {
        mockMvc.perform(get("/api/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
