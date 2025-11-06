package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionUpdateV2Request;
import com.fa25se225.capstone.dto.v2.response.QuestionV2Response;

import java.util.List;

public interface QuestionV2Service {
    QuestionV2Response createQuestion(QuestionCreationV2Request request);
    QuestionV2Response updateQuestion(String id, QuestionUpdateV2Request request);
    QuestionV2Response getQuestionById(String id);
    PageResponse<List<QuestionV2Response>> getAllQuestions(int pageNo, int pageSize, String... sorts);
    PageResponse<List<QuestionV2Response>> getQuestionsBySubject(String subjectId, int pageNo, int pageSize, String... sorts);
    PageResponse<List<QuestionV2Response>> getQuestionsByTopic(String topicId, int pageNo, int pageSize, String... sorts);
    PageResponse<List<QuestionV2Response>> getQuestionsByCreatedBy(String userId, int pageNo, int pageSize, String... sorts);
    PageResponse<List<QuestionV2Response>> searchQuestions(String keyword, int pageNo, int pageSize, String... sorts);
    void deleteQuestion(String id);
}
