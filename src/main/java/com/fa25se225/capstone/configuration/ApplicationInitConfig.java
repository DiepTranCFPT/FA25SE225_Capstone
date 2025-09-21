package com.fa25se225.capstone.configuration;

import com.fa25se225.capstone.constant.PredefinedRole;
import com.fa25se225.capstone.entity.Role;
import com.fa25se225.capstone.entity.User;
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

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    static String ADMIN_EMAIL = "admin123@gmail.com";
    static String ADMIN_PASSWORD = "admin123";


    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("Initializing application for default user and roles...");

        return args -> {
            if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
                log.info("Admin user not found, creating default roles and admin user...");

                Role adminRole = roleRepository.findById(PredefinedRole.ADMIN_ROLE)
                        .orElseGet(() -> roleRepository.save(Role.builder()
                                .name(PredefinedRole.ADMIN_ROLE)
                                .description("Admin role")
                                .build()));

                Role userRole = roleRepository.findById(PredefinedRole.USER_ROLE)
                        .orElseGet(() -> roleRepository.save(Role.builder()
                                .name(PredefinedRole.USER_ROLE)
                                .description("User role")
                                .build()));





                User adminUser = User.builder()
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .email(ADMIN_EMAIL)
                        .firstName("Admin")
                        .lastName("System")
                        .emailVerified(true)
                        .roles(Set.of(adminRole, userRole))
                        .build();

                userRepository.save(adminUser);
                log.warn("Default admin user '{}' has been created with password '{}'. Please change it immediately!", ADMIN_EMAIL, ADMIN_PASSWORD);
            }
            log.info("Application initialization completed.");
        };
    }
}