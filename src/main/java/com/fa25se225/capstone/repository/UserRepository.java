package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Role;
import com.fa25se225.capstone.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndVerificationToken(String email, String token);

    @Query("SELECT u FROM User u join TeacherProfile tp on u.id = tp.user.id left JOIN u.roles r WHERE r.name = :roleName and tp.isVerified = false")
    List<User> findUnverifiedTeachers(@Param("roleName") String roleName);


    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.createdAt BETWEEN :start AND :end")
    long countNewUsersByRole(@Param("roleName") String roleName, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countTotalByRole(@Param("roleName") String roleName);

    Page<User> findAllByRoles(Set<Role> roles, Pageable pageable);

    Page<User> findAllByRolesAndDeletedFalse(Set<Role> roles, Pageable pageable);

    Page<User> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND NOT EXISTS (SELECT r FROM u.roles r WHERE r.name = 'ADMIN')")
    Page<User> findAllByDeletedFalseAndRoleNotAdmin(Pageable pageable);

    List<User> findByGrantedPermissions_Name(String permissionName);
}