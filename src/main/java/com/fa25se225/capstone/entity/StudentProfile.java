package com.fa25se225.capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Student_Profile")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enroller_subject")
    private String enrolledSubject;

    @OneToMany(fetch = FetchType.LAZY)
    private List<ConversationAI> conversationAI;

    @OneToMany(fetch = FetchType.LAZY)
    private List<GradeLevel> gradeLevel;

    @Override
    public String toString() {
        return "StudentProfile{" +
                "id=" + id +
                ", enrolledSubject='" + enrolledSubject + '\'' +
                ", conversationAI=" + conversationAI +
                ", gradeLevel=" + gradeLevel +
                '}';
    }
}
