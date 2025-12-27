package com.fa25se225.capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TeacherReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_profile_id", nullable = false) // ✅ FIX: name rõ ràng
    private TeacherProfile teacherProfile;

    private int syllabusAlignment; // 1-5
    private int conceptAccuracy;   // 1-5
    private int difficultyFit;     // 1-5
    private int explanationQuality;// 1-5

    private String recommendation; // Qualified | Partially qualified | Not qualified

    @Lob
    @Column(columnDefinition = "TEXT")
    private String feedback;

    private String reviewerType = "AI"; // AI | HUMAN (để mở rộng)
    private boolean latest = true;      // đánh dấu review mới nhất

    private Instant createdAt = Instant.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TeacherProfile getTeacherProfile() {
        return teacherProfile;
    }

    public void setTeacherProfile(TeacherProfile teacherProfile) {
        this.teacherProfile = teacherProfile;
    }

    public int getSyllabusAlignment() {
        return syllabusAlignment;
    }

    public void setSyllabusAlignment(int syllabusAlignment) {
        this.syllabusAlignment = syllabusAlignment;
    }

    public int getConceptAccuracy() {
        return conceptAccuracy;
    }

    public void setConceptAccuracy(int conceptAccuracy) {
        this.conceptAccuracy = conceptAccuracy;
    }

    public int getDifficultyFit() {
        return difficultyFit;
    }

    public void setDifficultyFit(int difficultyFit) {
        this.difficultyFit = difficultyFit;
    }

    public int getExplanationQuality() {
        return explanationQuality;
    }

    public void setExplanationQuality(int explanationQuality) {
        this.explanationQuality = explanationQuality;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getReviewerType() {
        return reviewerType;
    }

    public void setReviewerType(String reviewerType) {
        this.reviewerType = reviewerType;
    }

    public boolean isLatest() {
        return latest;
    }

    public void setLatest(boolean latest) {
        this.latest = latest;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

