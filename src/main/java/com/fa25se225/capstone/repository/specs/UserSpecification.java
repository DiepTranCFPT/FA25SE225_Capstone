package com.fa25se225.capstone.repository.specs;

import com.fa25se225.capstone.dto.request.UserSearchRequest;
import com.fa25se225.capstone.entity.Role;
import com.fa25se225.capstone.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class UserSpecification {

    public static Specification<User> getSpec(UserSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.keyword())) {
                String likePattern = "%" + request.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), likePattern),
                        cb.like(cb.lower(root.get("firstName")), likePattern),
                        cb.like(cb.lower(root.get("lastName")), likePattern)
                ));
            }

            if (StringUtils.hasText(request.role())) {
                Join<User, Role> roleJoin = root.join("roles");
                query.distinct(true);
                predicates.add(cb.equal(roleJoin.get("name"), request.role()));
            }

            if (Objects.nonNull(request.isVerified())) {
                predicates.add(cb.equal(root.get("emailVerified"), request.isVerified()));
            }

            if (Objects.nonNull(request.isLocked())) {
                predicates.add(cb.equal(root.get("accountLocked"), request.isLocked()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
