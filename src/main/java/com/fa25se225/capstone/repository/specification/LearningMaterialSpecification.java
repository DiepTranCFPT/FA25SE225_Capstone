package com.fa25se225.capstone.repository.specification;

import com.fa25se225.capstone.entity.LearningMaterial;
import com.fa25se225.capstone.entity.TeacherProfile;
import com.fa25se225.capstone.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LearningMaterialSpecification {

    public static Specification<LearningMaterial> withFilters(
            Integer year,
            Integer month,
            Integer day,
            String subjectId,
            String typeId,
            String authorId,
            Integer minRating
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter out deleted materials
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            // Filter by year
            if (year != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.function("YEAR", Integer.class, root.get("createdAt")),
                        year
                ));
            }

            // Filter by month
            if (month != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.function("MONTH", Integer.class, root.get("createdAt")),
                        month
                ));
            }

            // Filter by day
            if (day != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.function("DAY", Integer.class, root.get("createdAt")),
                        day
                ));
            }

            // Filter by subject
            if (subjectId != null && !subjectId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("subject").get("id"), subjectId));
            }

            // Filter by type
            if (typeId != null && !typeId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("type").get("id"), typeId));
            }

            // Filter by author
            if (authorId != null && !authorId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("author").get("id"), authorId));
            }

            // Filter by teacher rating
            if (minRating != null) {
                // Join with author (User) and then with TeacherProfile
                Join<LearningMaterial, User> authorJoin = root.join("author", JoinType.INNER);
                Join<User, TeacherProfile> teacherProfileJoin = authorJoin.join("teacherProfile", JoinType.INNER);
                
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        teacherProfileJoin.get("rating"),
                        minRating
                ));
            }

            // Fetch joins to avoid N+1 problem
            if (query != null) {
                query.distinct(true);
                root.fetch("type", JoinType.LEFT);
                root.fetch("subject", JoinType.LEFT);
                Fetch<LearningMaterial, User> authorFetch = root.fetch("author", JoinType.LEFT);
                
                // Only fetch teacherProfile if filtering by rating
                if (minRating != null) {
                    authorFetch.fetch("teacherProfile", JoinType.LEFT);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
