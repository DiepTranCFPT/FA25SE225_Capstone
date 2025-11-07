package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.v2.request.ExamRuleV2Request;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamRuleV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamTemplateV2Response;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.ExamRuleV2;
import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import com.fa25se225.capstone.entity.v2.QuestionDifficultyV2;
import com.fa25se225.capstone.entity.v2.QuestionTopicV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.ExamRuleV2Mapper;
import com.fa25se225.capstone.mapper.v2.ExamTemplateV2Mapper;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.repository.v2.ExamRuleV2Repository;
import com.fa25se225.capstone.repository.v2.ExamTemplateV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionDifficultyV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionTopicV2Repository;
import com.fa25se225.capstone.service.v2.ExamTemplateV2Service;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamTemplateV2ServiceImpl implements ExamTemplateV2Service {

    private final ExamTemplateV2Repository templateRepository;
    private final ExamRuleV2Repository ruleRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionTopicV2Repository questionTopicV2Repository;
    private final QuestionDifficultyV2Repository questionDifficultyV2Repository;
    private final UserRepository userRepository;
    private final AccountUtil accountUtil;
    private final ExamTemplateV2Mapper templateMapper;
    private final ExamRuleV2Mapper ruleMapper;
    private final PageHelper pageHelper;

    @Override
    @Transactional
    public ExamTemplateV2Response createTemplate(ExamTemplateV2Request request) {
        log.info("Creating ExamTemplateV2: {}", request.getTitle());

        Subject subject = subjectRepository.findByNameIgnoreCase(request.getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        User currentUser = accountUtil.getCurrentUser();

        ExamTemplateV2 template = ExamTemplateV2.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .subject(subject)
                .createdBy(currentUser)
                .duration(request.getDuration())
                .passingScore(request.getPassingScore())
                .isActive(Objects.isNull(request.getIsActive()) ? true : request.getIsActive())
                .build();

        if (request.getRules() != null && !request.getRules().isEmpty()) {
            List<ExamRuleV2> rules = request.getRules().stream().map(r -> {
                QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(r.getTopicName())
                        .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));
                QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(r.getDifficultyName())
                        .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND));
                QuestionType qt;
                try {
                    qt = QuestionType.fromValue(r.getQuestionType().toUpperCase());
                } catch (Exception ex) {
                    throw new AppException(ErrorCode.INVALID_QUESTION_V2_TYPE);
                }
                ExamRuleV2 rule = ExamRuleV2.builder()
                        .template(template)
                        .topic(topic)
                        .difficulty(difficulty)
                        .numberOfQuestions(r.getNumberOfQuestions())
                        .questionType(qt)
                        .points(r.getPoints())
                        .build();
                return rule;
            }).collect(Collectors.toList());
            template.setRules(rules);
        }

        ExamTemplateV2 saved = templateRepository.save(template);
        log.info("Created ExamTemplateV2 id={}", saved.getId());
        return templateMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExamTemplateV2Response updateTemplate(String id, ExamTemplateV2Request request) {
        log.info("Updating ExamTemplateV2 id={}", id);
        ExamTemplateV2 existing = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));

        if (StringUtils.hasText(request.getTitle())) existing.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getDescription())) existing.setDescription(request.getDescription());
        if (Objects.nonNull(request.getDuration())) existing.setDuration(request.getDuration());
        if (Objects.nonNull(request.getPassingScore())) existing.setPassingScore(request.getPassingScore());
        if (Objects.nonNull(request.getIsActive())) existing.setIsActive(request.getIsActive());

        if (StringUtils.hasText(request.getSubject())) {
            Subject subject = subjectRepository.findByNameIgnoreCase(request.getSubject())
                    .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
            existing.setSubject(subject);
        }

        if (request.getRules() != null) {
            // remove existing rules and attach new ones
            existing.getRules().clear();
            List<ExamRuleV2> rules = request.getRules().stream().map(r -> {
                QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(r.getTopicName())
                        .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));
                QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(r.getDifficultyName())
                        .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND));
                QuestionType qt;
                try {
                    qt = QuestionType.fromValue(r.getQuestionType().toUpperCase());
                } catch (Exception ex) {
                    throw new AppException(ErrorCode.INVALID_QUESTION_V2_TYPE);
                }
                ExamRuleV2 rule = ExamRuleV2.builder()
                        .template(existing)
                        .topic(topic)
                        .difficulty(difficulty)
                        .numberOfQuestions(r.getNumberOfQuestions())
                        .questionType(qt)
                        .points(r.getPoints())
                        .build();
                return rule;
            }).collect(Collectors.toList());
            existing.setRules(rules);
        }

        ExamTemplateV2 saved = templateRepository.save(existing);
        return templateMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTemplate(String id) {
        if (!templateRepository.existsById(id)) {
            throw new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND);
        }
        templateRepository.deleteById(id);
    }

    @Override
    public ExamTemplateV2Response getTemplateById(String id) {
        ExamTemplateV2 template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));
        return templateMapper.toResponse(template);
    }

    @Override
    public PageResponse<List<ExamTemplateV2Response>> getAllTemplates(int pageNo, int pageSize, String... sorts) {
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<ExamTemplateV2> page = templateRepository.findAll(pageable);
        List<ExamTemplateV2Response> items = page.getContent().stream().map(templateMapper::toResponse).toList();
        return PageResponse.<List<ExamTemplateV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(page.getTotalElements())
                .totalPage(page.getTotalPages())
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public ExamRuleV2Response addRule(String templateId, ExamRuleV2Request request) {
        ExamTemplateV2 template = templateRepository.findById(templateId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));

        QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(request.getTopicName())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));
        QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(request.getDifficultyName())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND));
        QuestionType qt;
        try {
            qt = QuestionType.fromValue(request.getQuestionType().toUpperCase());
        } catch (Exception ex) {
            throw new AppException(ErrorCode.INVALID_QUESTION_V2_TYPE);
        }

        ExamRuleV2 rule = ExamRuleV2.builder()
                .template(template)
                .topic(topic)
                .difficulty(difficulty)
                .numberOfQuestions(request.getNumberOfQuestions())
                .questionType(qt)
                .points(request.getPoints())
                .build();

        ExamRuleV2 saved = ruleRepository.save(rule);
        // attach to template
        template.getRules().add(saved);
        templateRepository.save(template);
        return ruleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExamRuleV2Response updateRule(String ruleId, ExamRuleV2Request request) {
        ExamRuleV2 rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_QUESTION_NOT_FOUND));

        if (StringUtils.hasText(request.getTopicName())) {
            QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(request.getTopicName())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));
            rule.setTopic(topic);
        }
        if (StringUtils.hasText(request.getDifficultyName())) {
            QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(request.getDifficultyName())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND));
            rule.setDifficulty(difficulty);
        }
        if (StringUtils.hasText(request.getQuestionType())) {
            try {
                rule.setQuestionType(QuestionType.fromValue(request.getQuestionType().toUpperCase()));
            } catch (Exception ex) {
                throw new AppException(ErrorCode.INVALID_QUESTION_V2_TYPE);
            }
        }
        if (Objects.nonNull(request.getNumberOfQuestions())) rule.setNumberOfQuestions(request.getNumberOfQuestions());
        if (Objects.nonNull(request.getPoints())) rule.setPoints(request.getPoints());

        ExamRuleV2 saved = ruleRepository.save(rule);
        return ruleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRule(String ruleId) {
        if (!ruleRepository.existsById(ruleId)) {
            throw new AppException(ErrorCode.EXAM_QUESTION_NOT_FOUND);
        }
        ruleRepository.deleteById(ruleId);
    }
}

