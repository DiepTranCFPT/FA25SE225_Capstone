package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.dto.v2.request.QuestionDifficultyV2Request;
import com.fa25se225.capstone.dto.v2.response.QuestionDifficultyV2Response;
import com.fa25se225.capstone.entity.v2.QuestionDifficultyV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.QuestionDifficultyV2Mapper;
import com.fa25se225.capstone.repository.v2.QuestionDifficultyV2Repository;
import com.fa25se225.capstone.service.v2.QuestionDifficultyV2Service;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class QuestionDifficultyV2ServiceImpl implements QuestionDifficultyV2Service {

    private final QuestionDifficultyV2Repository questionDifficultyV2Repository;
    private final QuestionDifficultyV2Mapper questionDifficultyV2Mapper;

    @Override
    public QuestionDifficultyV2Response createDifficulty(QuestionDifficultyV2Request request) {
        if(questionDifficultyV2Repository.existsByNameIgnoreCase(request.getName())){
            throw new AppException(ErrorCode.QUESTION_DIFFICULTY_EXISTED);
        }
        var savedDifficulty = questionDifficultyV2Repository.save(questionDifficultyV2Mapper.toEntity(request));
        return questionDifficultyV2Mapper.toResponse(savedDifficulty);
    }

    @Override
    public List<QuestionDifficultyV2Response> getAllDifficulties() {
        return questionDifficultyV2Repository.findAll().stream().map(questionDifficultyV2Mapper::toResponse).toList();
    }

    @Override
    public QuestionDifficultyV2Response updateDifficulty(String id, QuestionDifficultyV2Request request) {
        var savedDifficulty = getQuestionDifficultyByIDOrThrowException(id);
        if(questionDifficultyV2Repository.existsByNameIgnoreCase(request.getName())){
            throw new AppException(ErrorCode.QUESTION_DIFFICULTY_EXISTED);
        }

        questionDifficultyV2Mapper.updateEntity(savedDifficulty, request);

        var updatedE = questionDifficultyV2Repository.save(savedDifficulty);

        return questionDifficultyV2Mapper.toResponse(updatedE);
    }

    @Override
    public void deleteDifficulty(String id) {
        var difficulty = getQuestionDifficultyByIDOrThrowException(id);
        questionDifficultyV2Repository.delete(difficulty);
    }

    private QuestionDifficultyV2 getQuestionDifficultyByIDOrThrowException(String id){
        return questionDifficultyV2Repository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND)
        );
    }
}
