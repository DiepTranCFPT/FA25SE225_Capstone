package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.v2.request.ExamRuleV2Request;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateUpdateV2Request;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamRuleV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamTemplateRatingResponse;
import com.fa25se225.capstone.dto.v2.response.ExamTemplateV2Response;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.ExamRuleV2Mapper;
import com.fa25se225.capstone.mapper.v2.ExamTemplateV2Mapper;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.v2.*;
import com.fa25se225.capstone.service.v2.ExamTemplateV2Service;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamTemplateV2ServiceImpl implements ExamTemplateV2Service {

    private final ExamTemplateV2Repository templateRepository;
    private final ExamRuleV2Repository ruleRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionTopicV2Repository questionTopicV2Repository;
    private final QuestionDifficultyV2Repository questionDifficultyV2Repository;
    private final AccountUtil accountUtil;
    private final ExamTemplateV2Mapper templateMapper;
    private final ExamRuleV2Mapper ruleMapper;
    private final PageHelper pageHelper;
    private final QuestionV2Repository questionV2Repository;
    private final ExamAttemptV2Repository examAttemptV2Repository;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    @CacheEvict(value = "exam_templates", allEntries = true)
    public ExamTemplateV2Response createTemplate(ExamTemplateV2Request request) {
        log.info("Creating ExamTemplateV2: {}", request.getTitle());
        ExamTemplateV2 template = templateMapper.toEntity(request);
        template.setSubject(getSubjectById(request.getSubjectId()));
        User currentUser = accountUtil.getCurrentUser();
        template.setCreatedBy(currentUser);
        if (template.getIsActive() == null) {
            template.setIsActive(true);
        }
        if (request.getRules() != null && !request.getRules().isEmpty()) {
            List<ExamRuleV2> rules = createRulesFromRequest(request.getRules(), template, currentUser);
            template.setRules(rules);
        }
        ExamTemplateV2 saved = templateRepository.save(template);
        log.info("Created ExamTemplateV2 id={}", saved.getId());
        return templateMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "exam_template", key = "#id"),
            @CacheEvict(value = "exam_templates", allEntries = true)
    })
    public ExamTemplateV2Response updateTemplate(String id, ExamTemplateUpdateV2Request request) {
        log.info("Updating ExamTemplateV2 id={}", id);
        ExamTemplateV2 existing = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));

        User teacher = existing.getCreatedBy();

        templateMapper.updateEntity(existing, request);
        if (StringUtils.hasText(request.getSubjectId())) {
            existing.setSubject(getSubjectById(request.getSubjectId()));
        }
        if (request.getRules() != null) {
            updateRulesCollection(existing, request.getRules(), teacher);
        }
        ExamTemplateV2 saved = templateRepository.save(existing);
        return templateMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "exam_template", key = "#id"),
            @CacheEvict(value = "exam_templates", allEntries = true)
    })
    public void deleteTemplate(String id) {
        if (!templateRepository.existsById(id)) {
            throw new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND);
        }
        templateRepository.softDeleteById(id);
    }

    @Override
    @Cacheable(value = "exam_template", key = "#id")
    public ExamTemplateV2Response getTemplateById(String id) {
        ExamTemplateV2 template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));
        return templateMapper.toResponse(template);
    }

    @Override
    @Cacheable(value = "exam_templates", key = "#pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<ExamTemplateV2Response>> getAllTemplates(int pageNo, int pageSize, String[] sorts) {
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        
        // Repository findAll đã được override để lọc deleted = false
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
    @Caching(evict = {
            @CacheEvict(value = "exam_template", key = "#templateId"),
            @CacheEvict(value = "exam_templates", allEntries = true)
    })
    public ExamRuleV2Response addRule(String templateId, ExamRuleV2Request request) {
        ExamTemplateV2 template = templateRepository.findById(templateId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));

        QuestionTopicV2 topic = getQuestionTopic(request.getTopicName());
        QuestionDifficultyV2 difficulty = getQuestionDifficulty(request.getDifficultyName());
        QuestionType questionType = getQuestionType(request.getQuestionType());
        User teacher = template.getCreatedBy();

        // Validate dựa trên số lượng context hoặc số lượng câu hỏi lẻ
        validateQuestionAvailability(topic.getId(), difficulty.getId(), questionType, teacher.getId(),
                request.getNumberOfQuestions(), request.getNumberOfContexts());

        ExamRuleV2 rule = ExamRuleV2.builder()
                .template(template)
                .topic(topic)
                .difficulty(difficulty)
                .numberOfQuestions(request.getNumberOfQuestions())
                .numberOfContexts(request.getNumberOfContexts() != null ? request.getNumberOfContexts() : 0) // Map field mới
                .questionType(questionType)
                .points(request.getPoints())
                .build();
        ExamRuleV2 saved = ruleRepository.save(rule);

        template.getRules().add(saved);
        templateRepository.save(template);
        return ruleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "exam_templates", allEntries = true)
    public ExamRuleV2Response updateRule(String ruleId, ExamRuleV2Request request) {
        ExamRuleV2 rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_RULE_NOT_FOUND));
        
        if (rule.getTemplate().isDeleted()) {
            throw new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND);
        }

        User teacher = rule.getTemplate().getCreatedBy();

        QuestionTopicV2 topic = StringUtils.hasText(request.getTopicName()) ?
                getQuestionTopic(request.getTopicName()) : rule.getTopic();

        QuestionDifficultyV2 difficulty = StringUtils.hasText(request.getDifficultyName()) ?
                getQuestionDifficulty(request.getDifficultyName()) : rule.getDifficulty();

        QuestionType questionType = StringUtils.hasText(request.getQuestionType()) ?
                getQuestionType(request.getQuestionType()) : rule.getQuestionType();

        Integer numberOfQuestions = Objects.nonNull(request.getNumberOfQuestions()) ?
                request.getNumberOfQuestions() : rule.getNumberOfQuestions();

        // Logic update cho numberOfContexts (field mới)
        Integer numberOfContexts = Objects.nonNull(request.getNumberOfContexts()) ?
                request.getNumberOfContexts() : rule.getNumberOfContexts();

        // Validate với data mới
        validateQuestionAvailability(
                topic.getId(),
                difficulty.getId(),
                questionType,
                teacher.getId(),
                numberOfQuestions,
                numberOfContexts
        );

        rule.setTopic(topic);
        rule.setDifficulty(difficulty);
        rule.setQuestionType(questionType);
        rule.setNumberOfQuestions(numberOfQuestions);
        rule.setNumberOfContexts(numberOfContexts); // Set value

        if (Objects.nonNull(request.getPoints())) {
            rule.setPoints(request.getPoints());
        }
        ExamRuleV2 saved = ruleRepository.save(rule);

        // Evict cache for the parent template
        evictTemplateCache(saved.getTemplate().getId());

        return ruleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "exam_templates", allEntries = true)
    public void deleteRule(String ruleId) {
        ExamRuleV2 rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_RULE_NOT_FOUND));
        
        if (rule.getTemplate().isDeleted()) {
            throw new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND);
        }

        String templateId = rule.getTemplate().getId();
        ruleRepository.delete(rule);

        // Evict cache for the parent template
        evictTemplateCache(templateId);
    }

    @Override
    @Cacheable(value = "exam_templates", key = "'browse_' + #subjectId + '_' + #teacherId + '_' + #minRating + '_' + #pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<ExamTemplateV2Response>> browseActiveTemplates(
            String subjectId, String teacherId, double minRating,
            int pageNo, int pageSize, String[] sorts) {

        if (sorts != null) {
            for (int i = 0; i < sorts.length; i++) {
                if (sorts[i] != null && sorts[i].contains("isVerified")) {
                    sorts[i] = sorts[i].replace("isVerified", "isTeacherVerified");
                }
            }
        }

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);

        Specification<ExamTemplateV2> spec = ExamTemplateSpecification.findActiveWithFilters(
                subjectId, teacherId, minRating
        );
        
        // Add deleted = false condition
        spec = spec.and((root, query, cb) -> cb.equal(root.get("deleted"), false));

        Page<ExamTemplateV2> page = templateRepository.findAll(spec, pageable);

        List<ExamTemplateV2Response> items = page.getContent().stream()
                .map(templateMapper::toResponse)
                .toList();

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
    @Cacheable(value = "exam_templates", key = "'user_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_' + #pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<ExamTemplateV2Response>> getTemplatesByCurrentUser(
            int pageNo, int pageSize, String[] sorts) {

        User currentUser = accountUtil.getCurrentUser();
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);

        Page<ExamTemplateV2> page = templateRepository.findByCreatedById(currentUser.getId(), pageable);
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
    @Cacheable(value = "exam_template_ratings", key = "#id + '_' + #pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<ExamTemplateRatingResponse>> getRatingById(String id, int pageNo, int pageSize, String[] sorts) {
        // Check if template exists and is not deleted
        if (!templateRepository.existsById(id)) {
             throw new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND);
        }
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<ExamAttemptV2> page = examAttemptV2Repository.findBySourceTemplateIdAndRatingNotNull(id, pageable);
        List<ExamTemplateRatingResponse> items = page.getContent().stream()
                .map(attempt -> ExamTemplateRatingResponse.builder()
                        .rating(attempt.getRating())
                        .comment(attempt.getComment())
                        .ratingTime(attempt.getRatingTime())
                        .rateBy(ExamTemplateRatingResponse.Student.builder()
                                .id(attempt.getUser().getId())
                                .firstName(attempt.getUser().getFirstName())
                                .lastName(attempt.getUser().getLastName())
                                .email(attempt.getUser().getEmail())
                                .imgUrl(attempt.getUser().getImgUrl())
                                .dob(attempt.getUser().getDob())
                                .build())
                        .build())
                .toList();
        return PageResponse.<List<ExamTemplateRatingResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(page.getTotalElements())
                .totalPage(page.getTotalPages())
                .items(items)
                .build();
    }

    private void evictTemplateCache(String templateId) {
        if (templateId != null && cacheManager.getCache("exam_template") != null) {
            cacheManager.getCache("exam_template").evict(templateId);
        }
    }


//    private void validateQuestionAvailability(String topicId, String difficultyId, QuestionType type, String creatorId, int requestedQuestions, Integer requestedContexts) {
//        log.debug("Validating question availability: topic={}, diff={}, type={}, creator={}, requestedQ={}, requestedCtx={}",
//                topicId, difficultyId, type, creatorId, requestedQuestions, requestedContexts);
//
//        int contextCount = (requestedContexts != null) ? requestedContexts : 0;
//
//        if (contextCount > 0) {
//            long availableContexts = questionV2Repository.countContextsAvailable(topicId, difficultyId, type, creatorId);
//            if (availableContexts < contextCount) {
//                log.warn("Insufficient contexts. Available: {}, Requested: {}", availableContexts, contextCount);
//                throw new AppException(ErrorCode.INSUFFICIENT_CONTEXTS_IN_BANK);
//            }
//        } else {
//            long availableCount = questionV2Repository.countSingleQuestionsAvailable(topicId, difficultyId, type, creatorId);
//
//            if (availableCount < requestedQuestions) {
//                log.warn("Insufficient single questions. Available: {}, Requested: {}", availableCount, requestedQuestions);
//                throw new AppException(ErrorCode.INSUFFICIENT_QUESTIONS_IN_BANK);
//            }
//        }
//    }

    private void validateQuestionAvailability(String topicId, String difficultyId, QuestionType type, String creatorId, int requestedQuestions, Integer requestedContexts) {
        log.debug("Validating question availability: topic={}, diff={}, type={}, creator={}, requestedQ={}, requestedCtx={}",
                topicId, difficultyId, type, creatorId, requestedQuestions, requestedContexts);

        int contextCount = (requestedContexts != null) ? requestedContexts : 0;

        if (contextCount > 0) {
            // Case 1: Chế độ Explicit (User yêu cầu chính xác số lượng bài đọc)
            long availableContexts = questionV2Repository.countContextsAvailable(topicId, difficultyId, type, creatorId);

            if (availableContexts < contextCount) {
                log.warn("Insufficient contexts. Available: {}, Requested: {}", availableContexts, contextCount);
                throw new AppException(ErrorCode.INSUFFICIENT_CONTEXTS_IN_BANK);
            }
        } else {

            long totalQuestionsAvailable = questionV2Repository.countTotalQuestionsAvailable(topicId, difficultyId, type.getValue(), creatorId);

            if (totalQuestionsAvailable < requestedQuestions) {
                log.warn("Insufficient total questions (Context+Single). Available: {}, Requested: {}", totalQuestionsAvailable, requestedQuestions);
                throw new AppException(ErrorCode.INSUFFICIENT_QUESTIONS_IN_BANK);
            }
        }
    }

    private List<ExamRuleV2> createRulesFromRequest(List<ExamRuleV2Request> ruleRequests, ExamTemplateV2 template, User teacher) {
        return ruleRequests.stream().map(r -> {

            QuestionTopicV2 topic = getQuestionTopic(r.getTopicName());
            QuestionDifficultyV2 difficulty = getQuestionDifficulty(r.getDifficultyName());
            QuestionType questionType = getQuestionType(r.getQuestionType());

            validateQuestionAvailability(topic.getId(), difficulty.getId(), questionType, teacher.getId(),
                    r.getNumberOfQuestions(), r.getNumberOfContexts());

            ExamRuleV2 rule = ruleMapper.toEntity(r);
            rule.setTemplate(template);
            rule.setTopic(topic);
            rule.setDifficulty(difficulty);
            rule.setQuestionType(questionType);

            if (r.getNumberOfContexts() != null) {
                rule.setNumberOfContexts(r.getNumberOfContexts());
            } else {
                rule.setNumberOfContexts(0);
            }

            return rule;
        }).toList();
    }

    private void updateRulesCollection(ExamTemplateV2 template, List<ExamRuleV2Request> newRuleRequests, User teacher) {
        List<ExamRuleV2> currentRules = template.getRules();
        currentRules.clear();
        templateRepository.flush();

        List<ExamRuleV2> newRules = createRulesFromRequest(newRuleRequests, template, teacher);
        currentRules.addAll(newRules);
    }

    private Subject getSubjectById(String subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
    }

    private QuestionTopicV2 getQuestionTopic(String name) {
        return questionTopicV2Repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));
    }

    private QuestionDifficultyV2 getQuestionDifficulty(String name) {
        return questionDifficultyV2Repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND));
    }

    private QuestionType getQuestionType(String value) {
        try {
            return QuestionType.fromValue(value.toUpperCase());
        } catch (Exception ex) {
            throw new AppException(ErrorCode.INVALID_QUESTION_V2_TYPE);
        }
    }
}
