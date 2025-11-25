package com.fa25se225.capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teacher_profiles")
public class TeacherProfile {
    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "experience")
    private String experience;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @Column(name = "rating")
    private Integer rating;

    @ElementCollection
    @CollectionTable(
        name = "teacher_certificates",
        joinColumns = @JoinColumn(name = "teacher_id")
    )
    @Column(name = "certificate_url")
    private List<String> certificateUrls = new ArrayList<>();

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

}
