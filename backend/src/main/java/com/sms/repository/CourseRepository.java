package com.sms.repository;

import com.sms.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    List<Course> findByFacultyId(Long facultyId);
    List<Course> findBySemester(Integer semester);
}
