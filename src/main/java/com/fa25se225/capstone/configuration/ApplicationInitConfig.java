package com.fa25se225.capstone.configuration;

import com.fa25se225.capstone.constant.PredefinedSystemPermission;
import com.fa25se225.capstone.constant.PredefinedSystemRole;
import com.fa25se225.capstone.entity.Permission;
import com.fa25se225.capstone.entity.Role;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.PermissionRepository;
import com.fa25se225.capstone.repository.RoleRepository;
import com.fa25se225.capstone.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
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


    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        PermissionRepository permissionRepository) {
        return args -> {

            log.info("Seeding permissions...");
            for (PredefinedSystemPermission permEnum : PredefinedSystemPermission.values()) {
                createPermissionIfNotFound(permissionRepository, permEnum);
            }

            log.info("Seeding roles and permissions mapping...");
            for (PredefinedSystemRole roleEnum : PredefinedSystemRole.values()) {
                createRoleIfNotFound(roleRepository, permissionRepository, roleEnum);
            }

            createAdminUserIfNotFound(userRepository, roleRepository);

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

    private void createAdminUserIfNotFound(UserRepository userRepository, RoleRepository roleRepository) {
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
            userRepository.save(adminUser);
            log.warn("Default admin user '{}' created with password '{}'. PLEASE CHANGE THIS PASSWORD IN A PRODUCTION ENVIRONMENT!", ADMIN_EMAIL, ADMIN_PASSWORD);
        }
    }
}