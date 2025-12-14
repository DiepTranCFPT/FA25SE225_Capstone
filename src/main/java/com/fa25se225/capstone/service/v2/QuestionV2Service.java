package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionImportRequest;
import com.fa25se225.capstone.dto.v2.request.QuestionUpdateV2Request;
import com.fa25se225.capstone.dto.v2.response.QuestionImportResponse;
import com.fa25se225.capstone.dto.v2.response.QuestionManageV2Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuestionV2Service {
    QuestionManageV2Response createQuestion(QuestionCreationV2Request request);
    QuestionManageV2Response updateQuestion(String id, QuestionUpdateV2Request request);
    QuestionManageV2Response getQuestionById(String id);
    PageResponse<List<QuestionManageV2Response>> getAllQuestions(int pageNo, int pageSize, String... sorts);
    PageResponse<List<QuestionManageV2Response>> getQuestionsBySubject(String subjectId, int pageNo, int pageSize, String... sorts);
    PageResponse<List<QuestionManageV2Response>> getQuestionsByTopic(String topicId, int pageNo, int pageSize, String... sorts);
    PageResponse<List<QuestionManageV2Response>> getQuestionsByCreatedBy(String userId, int pageNo, int pageSize, String... sorts);
    PageResponse<List<QuestionManageV2Response>> searchQuestions(String keyword, int pageNo, int pageSize, String... sorts);
    void deleteQuestion(String id);
    void deleteQuestions(List<String> ids);

    // Import functionality
    QuestionImportResponse importQuestionsFromExcel(MultipartFile file, QuestionImportRequest request);
    byte[] generateExampleTemplate();
}
