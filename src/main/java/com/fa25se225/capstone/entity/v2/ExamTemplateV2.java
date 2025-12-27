package com.fa25se225.capstone.entity.v2;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam_templates_v2")
public class ExamTemplateV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private Integer passingScore;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private BigDecimal tokenCost;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamRuleV2> rules = new ArrayList<>();

    @Formula("(SELECT COALESCE(SUM(er.points_per_question * er.num_questions), 0) FROM exam_rules_v2 er WHERE er.template_id = id)")
    private Double maxScore;

    @Column(name = "average_rating", columnDefinition = "DECIMAL(3,2) default 0.0")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "total_takers")
    @Builder.Default
    private Integer totalTakers = 0;

    @CreationTimestamp
    private LocalDateTime createAt;
    @UpdateTimestamp
    private LocalDateTime updateAt;

    @Column(name = "score_mapping", columnDefinition = "TEXT")
    private String scoreMapping;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Formula("(SELECT tp.is_verified FROM teacher_profiles tp WHERE tp.user_id = created_by)")
    private Boolean isTeacherVerified;
}