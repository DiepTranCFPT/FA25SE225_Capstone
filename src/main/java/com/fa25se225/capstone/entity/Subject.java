package com.fa25se225.capstone.entity;

import com.fa25se225.capstone.utils.CodeGenerator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    @Builder.Default
    private String code = CodeGenerator.generateRandomCode();

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "subject")
    private List<Exam> exams = new ArrayList<>();

    @OneToMany(mappedBy = "subject")
    private List<LearningMaterial> learningMaterials = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = createdAt;
        if (deleted == null) deleted = false;
        if (code == null || code.isEmpty()) {
            code = CodeGenerator.generateRandomCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}
