package com.sms.repository;

import com.sms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);
    List<User> findByCoursesId(Long courseId);

    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.branch = :branch")
    List<User> findStudentsByBranch(String branch);
}
