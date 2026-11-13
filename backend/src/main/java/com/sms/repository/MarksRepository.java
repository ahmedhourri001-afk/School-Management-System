package com.sms.repository;

import com.sms.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarksRepository extends JpaRepository<Marks, Long> {
    List<Marks> findByStudentId(Long studentId);
    List<Marks> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Marks> findByCourseId(Long courseId);
}
