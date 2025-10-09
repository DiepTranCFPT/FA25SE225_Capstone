package com.fa25se225.capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User implements UserDetails {
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
    @Builder.Default
    boolean deleted = false;
    String verificationToken;
    @Builder.Default
    boolean emailVerified = false;
    @Builder.Default
    int failedLoginAttempts = 0;
    @Builder.Default
    boolean accountLocked = false;
    Instant lockTime;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<Role> roles;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false, nullable = false)
    Instant createdAt;

    @UpdateTimestamp
    Instant updateOn;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(
                role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (accountLocked && lockTime != null) {
            if (Instant.now().isAfter(lockTime.plus(24, ChronoUnit.HOURS))) {
                accountLocked = false;
                failedLoginAttempts = 0;
                lockTime = null;
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}
