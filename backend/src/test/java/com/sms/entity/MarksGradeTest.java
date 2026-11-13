package com.sms.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Test unitaire pur (sans Spring ni base de données) du calcul automatique de la note. */
class MarksGradeTest {

    private String gradeFor(double obtained, double total) {
        Marks marks = Marks.builder().marksObtained(obtained).totalMarks(total).build();
        marks.onSave(); // méthode protected, accessible car le test est dans le même package
        return marks.getGrade();
    }

    @Test
    void gradeBoundaries() {
        assertEquals("A+", gradeFor(90, 100));
        assertEquals("A", gradeFor(80, 100));
        assertEquals("B", gradeFor(70, 100));
        assertEquals("C", gradeFor(60, 100));
        assertEquals("D", gradeFor(50, 100));
        assertEquals("F", gradeFor(49.9, 100));
    }

    @Test
    void gradeUsesPercentageNotRawMarks() {
        assertEquals("A+", gradeFor(18, 20));
        assertEquals("F", gradeFor(5, 20));
    }
}
