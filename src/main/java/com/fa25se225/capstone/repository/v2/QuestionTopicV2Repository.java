package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.QuestionTopicV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionTopicV2Repository extends JpaRepository<QuestionTopicV2, String> {
    Optional<QuestionTopicV2> findByName(String name);
}
