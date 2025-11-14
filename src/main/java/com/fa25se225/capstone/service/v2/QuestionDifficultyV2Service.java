package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.v2.request.QuestionDifficultyV2Request;
import com.fa25se225.capstone.dto.v2.response.QuestionDifficultyV2Response;
import jakarta.validation.Valid;

import java.util.List;

public interface QuestionDifficultyV2Service {
    QuestionDifficultyV2Response createDifficulty(@Valid QuestionDifficultyV2Request request);

    List<QuestionDifficultyV2Response> getAllDifficulties();

    QuestionDifficultyV2Response updateDifficulty(String id, QuestionDifficultyV2Request request);

    void deleteDifficulty(String id);
}
