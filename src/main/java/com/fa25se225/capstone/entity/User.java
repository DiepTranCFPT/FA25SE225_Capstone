package com.fa25se225.capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String username;
    String password;
    String firstName;
    String lastName;
    LocalDate dob;
    String imgUrl;
    boolean deleted = false;
    private boolean emailVerified = false;
    private int failedLoginAttempts = 0;
    private boolean accountLocked = false;
    private LocalDateTime lockTime;

    @ManyToMany
    @Enumerated(EnumType.STRING)
    Set<Role> roles;
}
