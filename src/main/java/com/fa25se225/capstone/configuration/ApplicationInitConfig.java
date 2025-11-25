package com.fa25se225.capstone.configuration;

import com.fa25se225.capstone.configuration.dataseed.DataSeederV2;
import com.fa25se225.capstone.constant.PredefinedSystemPermission;
import com.fa25se225.capstone.constant.PredefinedSystemRole;
import com.fa25se225.capstone.entity.*;
import com.fa25se225.capstone.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    static String ADMIN_EMAIL = "admin123@gmail.com";
    static String ADMIN_PASSWORD = "admin123";

    static String TEACHER_EMAIL = "teacher@gmail.com";
    static String TEACHER_PASSWORD = "teacher123";

    static String PARENT_EMAIL = "parent@gmail.com";
    static String PARENT_PASSWORD = "parent123";

    static String STUDENT_EMAIL = "student@gmail.com";
    static String STUDENT_PASSWORD = "student123";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        PermissionRepository permissionRepository,
                                        DataSeederV2 dataSeederV2,
                                        ParentProfileRepository parentProfileRepository,
                                        StudentProfileRepository studentProfileRepository,
                                        TeacherProfileRepository teacherProfileRepository) {
        return args -> {

            log.info("Seeding permissions...");
            for (PredefinedSystemPermission permEnum : PredefinedSystemPermission.values()) {
                createPermissionIfNotFound(permissionRepository, permEnum);
            }

            log.info("Seeding roles and permissions mapping...");
            for (PredefinedSystemRole roleEnum : PredefinedSystemRole.values()) {
                createRoleIfNotFound(roleRepository, permissionRepository, roleEnum);
            }

            createUsersIfNotFound(userRepository, roleRepository, parentProfileRepository, studentProfileRepository, teacherProfileRepository);

            dataSeederV2.createExamDataSeed();
            log.info("Application data seeding finished.");
        };
    }

    private void createPermissionIfNotFound(PermissionRepository permissionRepository, PredefinedSystemPermission predefinedSystemPermission) {
        permissionRepository.findById(predefinedSystemPermission.name()).orElseGet(() -> {
            log.info("Creating permission: {}", predefinedSystemPermission.name());
            return permissionRepository.save(new Permission(predefinedSystemPermission.name(),
                                                            predefinedSystemPermission.getPermissionDescription(),
                                                    false));
        });
    }

    private void createRoleIfNotFound(RoleRepository roleRepository, PermissionRepository permissionRepository, PredefinedSystemRole predefinedSystemRole) {
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(predefinedSystemRole.getPermissions()));
        roleRepository.findById(predefinedSystemRole.name()).ifPresentOrElse(
                role -> {
                    role.setPermissions(permissions);
                    roleRepository.save(role);
                },
                () -> {
                    log.info("Creating role: {}", predefinedSystemRole.name());
                    Role newRole = Role.builder()
                            .name(predefinedSystemRole.name())
                            .description(predefinedSystemRole.getDescription())
                            .permissions(permissions).build();
                    roleRepository.save(newRole);
                });
    }


    @Transactional
    protected void createUsersIfNotFound(UserRepository userRepository,
                                         RoleRepository roleRepository,
                                         ParentProfileRepository parentProfileRepository,
                                         StudentProfileRepository studentProfileRepository,
                                         TeacherProfileRepository teacherProfileRepository) {
        if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {

            Set<Role> adminRoles = new HashSet<>(roleRepository.findAllById(
                    Stream.of(PredefinedSystemRole.values()).map(Enum::name).collect(Collectors.toSet())
            ));
            User adminUser = User.builder()
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .firstName("Admin")
                    .lastName("System")
                    .emailVerified(true)
                    .roles(adminRoles)
                    .build();

            User teacher = User.builder()
                    .email(TEACHER_EMAIL)
                    .password(passwordEncoder.encode(TEACHER_PASSWORD))
                    .firstName("Teacher")
                    .lastName("System")
                    .emailVerified(true)
                    .roles(Set.of(roleRepository.findByName(PredefinedSystemRole.TEACHER.name())))
                    .build();

            User parent = User.builder()
                    .email(PARENT_EMAIL)
                    .password(passwordEncoder.encode(PARENT_PASSWORD))
                    .firstName("Parent")
                    .lastName("System")
                    .emailVerified(true)
                    .roles(Set.of(roleRepository.findByName(PredefinedSystemRole.PARENT.name())))
                    .build();

            User student = User.builder()
                    .email(STUDENT_EMAIL)
                    .password(passwordEncoder.encode(STUDENT_PASSWORD))
                    .firstName("student")
                    .lastName("System")
                    .emailVerified(true)
                    .roles(Set.of(roleRepository.findByName(PredefinedSystemRole.STUDENT.name())))
                    .build();

            userRepository.saveAll(List.of(adminUser,teacher, parent, student));

            parentProfileRepository.save(ParentProfile.builder().id(adminUser.getId()).user(adminUser).build());
            studentProfileRepository.save(StudentProfile.builder().id(adminUser.getId()).user(adminUser).build());
            teacherProfileRepository.save(TeacherProfile.builder().id(adminUser.getId()).user(adminUser).build());

            teacherProfileRepository.save(TeacherProfile.builder().id(teacher.getId()).user(teacher).build());

            parentProfileRepository.save(ParentProfile.builder().id(parent.getId()).user(parent).build());

            studentProfileRepository.save(StudentProfile.builder().id(student.getId()).user(student).build());



            log.warn("Default admin user '{}' created with password '{}'. PLEASE CHANGE THIS PASSWORD IN A PRODUCTION ENVIRONMENT!", ADMIN_EMAIL, ADMIN_PASSWORD);
            log.warn("Default teacher user '{}' created with password '{}'. PLEASE CHANGE THIS PASSWORD IN A PRODUCTION ENVIRONMENT!", TEACHER_EMAIL, TEACHER_PASSWORD);
            log.warn("Default parent user '{}' created with password '{}'. PLEASE CHANGE THIS PASSWORD IN A PRODUCTION ENVIRONMENT!", PARENT_EMAIL, PARENT_PASSWORD);
            log.warn("Default student user '{}' created with password '{}'. PLEASE CHANGE THIS PASSWORD IN A PRODUCTION ENVIRONMENT!", STUDENT_EMAIL, STUDENT_PASSWORD);

        }
    }


}