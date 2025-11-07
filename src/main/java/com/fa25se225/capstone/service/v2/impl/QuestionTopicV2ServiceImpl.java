package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.dto.v2.response.QuestionTopicV2Response;
import com.fa25se225.capstone.repository.v2.QuestionTopicV2Repository;
import com.fa25se225.capstone.service.v2.QuestionTopicV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionTopicV2ServiceImpl implements QuestionTopicV2Service {
    private final QuestionTopicV2Repository questionTopicV2Repository;

    @Override
    public List<QuestionTopicV2Response> getAllTopics() {
        return questionTopicV2Repository.findAll().stream()
                .map(topic -> QuestionTopicV2Response.builder()
                        .id(topic.getId())
                        .name(topic.getName())
                        .description(topic.getDescription())
                        .subject(topic.getSubject() != null ? topic.getSubject().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }
}

