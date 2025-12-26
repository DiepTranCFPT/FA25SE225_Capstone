package com.fa25se225.capstone.entity;


import com.fa25se225.capstone.constant.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "teacher_verification_requests",
        indexes = {
                @Index(name = "idx_tvr_user_status", columnList = "user_id,status"),
                @Index(name = "idx_tvr_status", columnList = "status")
        }
)
public class TeacherVerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private VerificationStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "ai_result_json", columnDefinition = "TEXT")
    private String aiResultJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    private Instant reviewedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = VerificationStatus.PENDING;
    }
}

