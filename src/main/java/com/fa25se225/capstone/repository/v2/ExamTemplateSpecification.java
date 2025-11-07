package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class ExamTemplateSpecification {

    public static Specification<ExamTemplateV2> findActiveWithFilters(
            String subjectName, String teacherId, double minRating) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("isActive")));

            if (StringUtils.hasText(subjectName)) {
                Join<ExamTemplateV2, Subject> subjectJoin = root.join("subject");
                predicates.add(cb.equal(subjectJoin.get("name"), subjectName));
            }

            if (StringUtils.hasText(teacherId)) {
                Join<ExamTemplateV2, User> teacherJoin = root.join("createdBy");
                predicates.add(cb.equal(teacherJoin.get("id"), teacherId));
            }

            if (minRating > 0.0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}