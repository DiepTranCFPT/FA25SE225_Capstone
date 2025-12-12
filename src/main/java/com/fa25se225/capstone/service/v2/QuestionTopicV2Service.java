package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2CreationRequest;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2UpdateRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionTopicV2Response;
import jakarta.validation.Valid;

import java.util.List;

public interface QuestionTopicV2Service {
    PageResponse<List<QuestionTopicV2Response>> getAllTopics(int pageNo, int pageSize, String... sorts);

    QuestionTopicV2Response createTopic(@Valid QuestionTopicV2CreationRequest request);

    QuestionTopicV2Response updateTopic(String topicId, @Valid QuestionTopicV2UpdateRequest request);

    void deleteTopic(String topicId);

    List<QuestionTopicV2Response> getTopicsBySubject(String subjectId);

    List<QuestionTopicV2Response> getTopicsByCurrentUser();

    QuestionTopicV2Response getTopicById(String topicId);
}
