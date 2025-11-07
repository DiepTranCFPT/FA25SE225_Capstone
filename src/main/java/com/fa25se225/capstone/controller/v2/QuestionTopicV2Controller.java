package com.fa25se225.capstone.controller.v2;

import com.fa25se225.capstone.dto.v2.response.QuestionTopicV2Response;
import com.fa25se225.capstone.service.v2.QuestionTopicV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/question-topics")
@RequiredArgsConstructor
public class QuestionTopicV2Controller {
    private final QuestionTopicV2Service questionTopicV2Service;

    @GetMapping
    public List<QuestionTopicV2Response> getAllTopics() {
        return questionTopicV2Service.getAllTopics();
    }
}
