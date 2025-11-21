package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailAndVerificationToken(String email, String token);

    // Find all users with role 'TEACHER' and non-null verificationToken (not verified)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.verificationToken IS NOT NULL")
    List<User> findUnverifiedTeachers(@Param("roleName") String roleName);
}
