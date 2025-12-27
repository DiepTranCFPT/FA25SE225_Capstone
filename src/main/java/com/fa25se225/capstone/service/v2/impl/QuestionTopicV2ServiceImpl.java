package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2CreationRequest;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2UpdateRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionTopicV2Response;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.QuestionTopicV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.QuestionTopicV2Mapper;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.v2.QuestionTopicV2Repository;
import com.fa25se225.capstone.service.v2.QuestionTopicV2Service;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionTopicV2ServiceImpl implements QuestionTopicV2Service {
    private final QuestionTopicV2Repository questionTopicV2Repository;
    private final QuestionTopicV2Mapper questionTopicV2Mapper;
    private final SubjectRepository subjectRepository;
    private final PageHelper pageHelper;
    private final AccountUtil accountUtil;


    @Override
    public PageResponse<List<QuestionTopicV2Response>> getAllTopics(int pageNo, int pageSize, String[] sorts) {
        log.info("Getting all topics with pagination - page: {}, size: {}", pageNo, pageSize);

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionTopicV2> page = questionTopicV2Repository.findAll(pageable);

        List<QuestionTopicV2Response> responses = page.getContent()
                .stream()
                .map(questionTopicV2Mapper::toResponse)
                .toList();

        return PageResponse.<List<QuestionTopicV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }

    @Override
    @Transactional
    public QuestionTopicV2Response createTopic(QuestionTopicV2CreationRequest request) {
        log.info("Creating new topic: {}", request.getName());


        User currentUser = accountUtil.getCurrentUser();

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        checkExistedQuestionByNameSubjectTeacher(request.getName(), request.getSubjectId(), currentUser.getId());

        QuestionTopicV2 topic = questionTopicV2Mapper.toEntity(request);
        topic.setSubject(subject);
        topic.setCreatedBy(currentUser);

        QuestionTopicV2 savedTopic = questionTopicV2Repository.save(topic);

        return questionTopicV2Mapper.toResponse(savedTopic);
    }

    @Override
    @Transactional
    public QuestionTopicV2Response updateTopic(String topicId, QuestionTopicV2UpdateRequest request) {

        User currentUser = accountUtil.getCurrentUser();

        QuestionTopicV2 existingTopic = questionTopicV2Repository.findById(topicId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));

        if (questionTopicV2Repository.existsByNameIgnoreCaseAndSubjectIdAndCreatedById(request.getName(), request.getSubjectId(), currentUser.getId())) {
            throw new AppException(ErrorCode.EXISTED_QUESTION_TOPIC);
        }

        if (Objects.nonNull(request.getSubjectId())) {
            Subject newSubject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
            existingTopic.setSubject(newSubject);
        }

        questionTopicV2Mapper.updateEntity(existingTopic, request);

        QuestionTopicV2 updatedTopic = questionTopicV2Repository.save(existingTopic);
        log.info("Topic updated successfully");

        return questionTopicV2Mapper.toResponse(updatedTopic);
    }

    private void checkExistedQuestionByNameSubjectTeacher(String topicName, String subjectId, String teacherId){
        if (questionTopicV2Repository.existsByNameIgnoreCaseAndSubjectIdAndCreatedById(topicName, subjectId, teacherId)) {
            throw new AppException(ErrorCode.EXISTED_QUESTION_TOPIC);
        }
    }

    @Override
    @Transactional
    public void deleteTopic(String topicId) {
        QuestionTopicV2 topic = questionTopicV2Repository.findById(topicId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));

        questionTopicV2Repository.delete(topic);
        log.info("Topic deleted successfully");
    }

    @Override
    public List<QuestionTopicV2Response> getTopicsBySubject(String subjectId) {
        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        List<QuestionTopicV2> topics = questionTopicV2Repository.findBySubjectId(subjectId);

        return topics.stream()
                .map(questionTopicV2Mapper::toResponse)
                .sorted(Comparator.comparing(
                        QuestionTopicV2Response::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .toList();
    }

    @Override
    public List<QuestionTopicV2Response> getTopicsByCurrentUser() {
        User currentUser = accountUtil.getCurrentUser();
        List<QuestionTopicV2> topics = questionTopicV2Repository.findByCreatedById(currentUser.getId());

        return topics.stream()
                .map(questionTopicV2Mapper::toResponse)
                .toList();
    }

    @Override
    public QuestionTopicV2Response getTopicById(String topicId) {
        QuestionTopicV2 topic = questionTopicV2Repository.findById(topicId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));

        return questionTopicV2Mapper.toResponse(topic);
    }

}
