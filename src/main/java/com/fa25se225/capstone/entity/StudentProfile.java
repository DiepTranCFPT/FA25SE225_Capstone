package com.fa25se225.capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
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
@Table(name = "student_profiles")
public class StudentProfile {
    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "school_name")
    private String schoolName;

    @Column(columnDefinition = "TEXT")
    @Nationalized
    private String goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_level_id")
    private GradeLevel gradeLevel;

    @Column(name = "parent_phone")
    private String parentPhone;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_profile_id")
    private AdvisorProfile advisorProfile;

    @ManyToMany(mappedBy = "children", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ParentProfile> parents = new ArrayList<>();

    @Column(name = "connection_code")
    private String connectionCode;

    @Column(name = "connection_code_expiry")
    private LocalDateTime connectionCodeExpiry;


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