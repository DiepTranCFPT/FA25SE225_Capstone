package com.fa25se225.capstone.entity.v2;

import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exams_v2")
public class ExamV2 {


    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private Integer passingScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belong_to", nullable = false)
    private User belongTo;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamQuestionV2> questions = new ArrayList<>();
}
