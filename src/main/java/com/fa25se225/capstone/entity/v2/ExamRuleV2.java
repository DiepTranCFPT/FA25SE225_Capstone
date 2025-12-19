package com.fa25se225.capstone.entity.v2;


import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.entity.QuestionDifficulty;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam_rules_v2")
public class ExamRuleV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ExamTemplateV2 template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "difficulty_id", nullable = false)
    private QuestionDifficultyV2 difficulty;

    @Column(name = "num_questions", nullable = false)
    private Integer numberOfQuestions;

    @Column(name = "number_of_contexts")
    private Integer numberOfContexts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private QuestionTopicV2 topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;

    @Column(name = "points_per_question", nullable = false)
    private Double points;

    @CreationTimestamp
    private LocalDateTime create_at;
    @UpdateTimestamp
    private LocalDateTime update_at;
}