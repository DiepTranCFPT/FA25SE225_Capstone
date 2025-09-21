package com.fa25se225.capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(unique = true, nullable = false)
    String email;

    @Column(nullable = false)
    String password;

    String firstName;
    String lastName;
    LocalDate dob;
    String imgUrl;
    boolean deleted = false;
    String verificationToken;
    boolean emailVerified = false;
    int failedLoginAttempts = 0;
    boolean accountLocked = false;
    LocalDateTime lockTime;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<Role> roles;

    @CreationTimestamp
    Instant createdOn;

    @UpdateTimestamp
    Instant updateOn;
}
