package com.fa25se225.capstone.mapper;



import com.fa25se225.capstone.dto.response.TeacherReviewResponse;
import com.fa25se225.capstone.entity.TeacherReview;
import org.springframework.stereotype.Component;

@Component
public class TeacherReviewMapper {
    public TeacherReviewResponse toResponse(TeacherReview r) {
        if (r == null) return null;
        return new TeacherReviewResponse(
                r.getId(),
                r.getSyllabusAlignment(),
                r.getConceptAccuracy(),
                r.getDifficultyFit(),
                r.getExplanationQuality(),
                r.getRecommendation(),
                r.getFeedback(),
                r.getReviewerType(),
                r.isLatest(),
                r.getCreatedAt()
        );
    }
}

