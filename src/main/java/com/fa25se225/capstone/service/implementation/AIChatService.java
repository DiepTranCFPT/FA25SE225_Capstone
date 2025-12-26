package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.request.AIGenerateQuestionRequest;
import com.fa25se225.capstone.dto.request.ExamAskingRequest;
import com.fa25se225.capstone.dto.v2.request.ExamRuleV2Request;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.entity.StudentProfile;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.QuestionDifficultyV2;
import com.fa25se225.capstone.entity.v2.QuestionTopicV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.StudentProfileRepository;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionDifficultyV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionTopicV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionV2Repository;
import com.fa25se225.capstone.service.StudentDashboardService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AIChatService {

    @Autowired
    @Qualifier("chatClientWithChatInMemory")
    private ChatClient primaryChatClient;

    @Autowired
    @Qualifier("chatClientWithoutChatMemory")
    private ChatClient primaryChatClientWithoutMemory;

    @Autowired
    @Qualifier("chatClientWithChatInMemoryUsingLiteModel")
    private ChatClient secondaryChatClient;

    @Autowired
    private InMemoryChatMemoryRepository inMemoryChatMemoryRepository;

    @Autowired
    private  AccountUtil accountUtil;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ExamAttemptV2Repository examAttemptV2Repository;

    @Autowired
    private StudentDashboardService studentDashboardService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private QuestionTopicV2Repository questionTopicV2Repository;

    @Autowired
    private QuestionDifficultyV2Repository questionDifficultyV2Repository;

    @Autowired
    private QuestionV2Repository questionV2Repository;


    private static class UserRequestInfo {
        final LocalDate date;
        final AtomicInteger count;

        UserRequestInfo(LocalDate date) {
            this.date = date;
            this.count = new AtomicInteger(0);
        }
    }

    private final ConcurrentHashMap<String, UserRequestInfo> dailyRequestMap = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 0 0 * * ?")
    protected void clearDailyRequests() {
        dailyRequestMap.clear();
    }


    private Set<String> conversationIds = new HashSet<>();


    public Flux<String> examAsk(ExamAskingRequest request){
        String conversationId = request.getDoneBy().concat(request.getAttemptId());
        conversationIds.add(conversationId);
        String questionContext = StringUtils.hasText(request.getQuestionContext()) ? request.getQuestionContext() : "";
        String userAsking = String.format(
                """
                        %s
                        Question : %s
                        Student answer : %s
                        Student asking : %s
                        """,
                questionContext, request.getQuestionContent(), request.getStudentAnswer(), request.getStudentAsking()
        );
        String systemText = """
                        You are an expert at answering AP exam questions. (Only answer questions that are related to the questions and answers students produce.)
                        **Special Characters**:
                        Return strictly valid JSON object.
                        - **LaTeX**: If the text contains LaTeX formulas (e.g., `\\frac`), ensure backslashes are escaped (e.g., `\\\\frac`).
                        - **Escaping**: Ensure all JSON strings are valid (escape double quotes `\\"` and backslashes `\\\\`), 
                        Escape all special characters (", \\, newlines, tabs). Do NOT use trailing commas or unescaped line breaks..
                """;

        return callChatClient(primaryChatClient, systemText, userAsking, conversationId)
                .onErrorResume(e -> {
                    log.warn("Primary model failed for conversation {}. Switching to Lite model. Error: {}", conversationId, e.getMessage());
                    return callChatClient(secondaryChatClient, systemText, userAsking, conversationId);
                });

    }


    @Scheduled(cron = "0 0 4 * * ?")
    protected void clearChatMemory(){
        conversationIds.stream().forEach(s -> inMemoryChatMemoryRepository.deleteByConversationId(s));
    }


    public Flux<String> chatStudentDashBoard(String prompt) {
        User student = accountUtil.getCurrentUser();
        StudentProfile studentProfile = studentProfileRepository.findByUserId(student.getId()).get();
        String conversationId = studentProfile.getId();
        conversationIds.add(conversationId);
        String studentInfo = studentDashboardService.getStudentExamDashboard().toString();
        String userAsking = String.format(
                """
                        Student Goal : %s
                        Student information : %s
                        Student asking : %s
                        
                        **Special Characters**:
                        Return strictly valid JSON object.
                        - **LaTeX**: If the text contains LaTeX formulas (e.g., `\\frac`), ensure backslashes are escaped (e.g., `\\\\frac`).
                        - **Escaping**: Ensure all JSON strings are valid (escape double quotes `\\"` and backslashes `\\\\`), 
                        Escape all special characters (", \\, newlines, tabs). Do NOT use trailing commas or unescaped line breaks..
                        """,
                studentProfile.getGoal(), studentInfo, prompt
        );
        String systemText = "You are an expert at answering AP exam questions. (Only answer questions that are related to the questions and answers students produce.)";

        return callChatClient(primaryChatClient, systemText, userAsking, conversationId)
                .onErrorResume(e -> {
                    log.warn("Primary model failed for conversation {}. Switching to Lite model. Error: {}", conversationId, e.getMessage());
                    return callChatClient(secondaryChatClient, systemText, userAsking, conversationId);
                });

    }

    @Scheduled(cron = "0 0 23 * * *")
    protected void createRecommendForStudents() {
        List<User> students = examAttemptV2Repository.findDistinctUsersAttemptedToday();
        for (User student : students) {
            setRecommendOfStudentByAI(student.getId());
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sleep interrupted while processing student {}: {}", student.getId(), e.getMessage());
            }

        }

    }

    @CacheEvict(value = "student_exam_dashboard", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public void createRecommendForCurrentStudent() {
        String userId = accountUtil.getCurrentUser().getId();
        AtomicBoolean allowed = new AtomicBoolean(false);
        LocalDate today = LocalDate.now();

        dailyRequestMap.compute(userId, (k, info) -> {
            if (info == null || !info.date.equals(today)) {
                UserRequestInfo newInfo = new UserRequestInfo(today);
                newInfo.count.incrementAndGet();
                allowed.set(true);
                return newInfo;
            }
            if (info.count.get() < 3) {
                info.count.incrementAndGet();
                allowed.set(true);
            }
            return info;
        });

        if (!allowed.get()) {
            log.warn("Daily recommend limit reached for user {} (max 3 per day).", userId);
            throw new AppException(ErrorCode.EXCEED_REQUEST);
        }

        setRecommendOfStudentByAI(userId);
    }

    public List<QuestionCreationV2Request> parseRawTextToQuestionJson(String subjectId, AIGenerateQuestionRequest request) {
        questionTopicV2Repository.findByNameIgnoreCase(request.getTopicName()).orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));
        String prompt = """
        You are an educational AI assistant. Your task is to parse the provided raw text containing exam questions into a structured JSON format.
        
        **INPUT TEXT:**
        %s
        
        **REQUIREMENTS:**
        1. **Structure**: Output a JSON Array containing a list of questions.
        2. **Schema**: Each question must match this Java DTO structure:
           - content (String): The question text.
           - type (String): "MCQ/FRQ" (if the question has only 1 answer and this answer is correct. it's FRQ).
           - subjectId (String): Use provided "%s".
           - answers (List): List of objects { "content": "...", "isCorrect": boolean, "explanation": "..." }.
           - context (Object): If the question belongs to a reading passage/scenario (e.g., "Questions 1-3 refer to..."), create this object with fields: "content" (the passage text), "title" (optional).
           - difficultyName (String): Infer difficulty (EASY/MEDIUM/HARD) (Default MEDIUM).
        
        **CRITICAL RULES:**
        1. **Context Parsing**: You MUST identify reading passages or group descriptions (e.g., "Questions 1-3 refer to the following information"). Extract that text into the `context.content` field for ALL questions in that group
        (If context do not have title, generate brief, simple title for it).
        2. **Answer Detection**: 
           - Look for markers like asterisk (*), bolding, or an answer key at the end. 
           - **IF NO CORRECT ANSWER IS INDICATED**: Set `isCorrect` to `false` for ALL options. 
           - If the user doesn't provide an explanation for the answer, you don't need to generate it yourself; leave it as is.
        3. **Output Format**: return ONLY valid JSON. Do not include markdown formatting (```json).
        4. **Special Characters**:
            Return strictly valid JSON object.
            - **LaTeX**: If the text contains LaTeX formulas (e.g., `\\frac`), ensure backslashes are escaped (e.g., `\\\\frac`).
            - **Escaping**: Ensure all JSON strings are valid (escape double quotes `\\"` and backslashes `\\\\`), 
            Escape all special characters (", \\, newlines, tabs). Do NOT use trailing commas or unescaped line breaks..
        
        **EXAMPLE JSON:**
        [
           {
             "content": "Which of the following...",
             "context": { "content": "I will be no man's tributary..." },
             "answers": [ ... ]
           }
        ]
        """;

        List<QuestionCreationV2Request> response = primaryChatClientWithoutMemory.prompt()
                .system(String.format(prompt, request.getRawText(), subjectId))
                .call()
                .entity(new ParameterizedTypeReference<List<QuestionCreationV2Request>>() {
                });
        response.forEach(question -> {
            question.setSubjectId(subjectId);
            question.getContext().setSubjectId(subjectId);
            question.setTopicName(request.getTopicName());
        });
        return response;
    }


    private void setRecommendOfStudentByAI(String studentId) {
        StudentProfile studentProfile = studentProfileRepository.findByUserId(studentId).get();
        String studentInfo = studentDashboardService.getStudentExamDashboard().toString();
        String userText = String.format(
                """
                        Student Goal : %s
                        Student information : %s
                        """,
                studentProfile.getGoal(), studentInfo
        );
        String systemText = """
                You are an AP exam expert. Analyze and guide students based on their information. (~100 words)
                If the student doesn't have a goal yet, still reply, but remind them to update their goal profile.
              
                """;

        String recommend =  primaryChatClientWithoutMemory.prompt().system(systemText)
                                            .user(userText)
                                            .call()
                                            .content();
        studentProfile.setRecommend(recommend);
        studentProfileRepository.save(studentProfile);
    }

    private Flux<String> callChatClient(ChatClient client, String systemText, String userText, String conversationId) {
        return client.prompt()
                .system(systemText)
                .user(userText)
                .advisors(advisorSpec -> advisorSpec.param("CONVERSATION_ID", conversationId))
                .stream()
                .content()
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(2))
                                .filter(this::isRetryable)
                                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying chat for conversation {} due to exception {}", conversationId, retrySignal.failure()))
                .onRetryExhaustedThrow((spec, sig) -> sig.failure())
                 );
    }

    private boolean isRetryable(Throwable ex) {
        return ex instanceof WebClientResponseException wex
                && (wex.getStatusCode().value() == 429 || wex.getStatusCode().is5xxServerError());
    }




    public String analyzeTemplateFeasibility(ExamTemplateV2Request request) {
        User currentUser = accountUtil.getCurrentUser();

        String subjectName = "Unknown";
        if (StringUtils.hasText(request.getSubjectId())) {
            subjectName = subjectRepository.findById(request.getSubjectId())
                    .map(Subject::getName).orElse("Unknown Subject");
        }

        StringBuilder rulesAnalysisReport = new StringBuilder();

        if (request.getRules() != null && !request.getRules().isEmpty()) {
            int ruleIndex = 1;
            for (ExamRuleV2Request rule : request.getRules()) {
                String topicName;
                String diffName;
                try {
                    topicName = getQuestionTopic(rule.getTopicName()).getName();
                    diffName = getQuestionDifficulty(rule.getDifficultyName()).getName();
                } catch (AppException e) {
                    rulesAnalysisReport.append(String.format("- Rule #%d: Invalid Topic or Difficulty configuration.\n", ruleIndex++));
                    continue;
                }

                QuestionType type = getQuestionType(rule.getQuestionType());
                int requestedQty = rule.getNumberOfQuestions();
                int requestedContexts = rule.getNumberOfContexts() != null ? rule.getNumberOfContexts() : 0;

                long available;
                String requirementDesc;
                String status;

                if (requestedContexts > 0) {
                    available = questionV2Repository.countContextsAvailable(
                            getQuestionTopic(rule.getTopicName()).getId(),
                            getQuestionDifficulty(rule.getDifficultyName()).getId(),
                            type,
                            currentUser.getId()
                    );
                    requirementDesc = String.format("Need %d Reading Passages (Contexts)", requestedContexts);

                    if (available < requestedContexts) {
                        status = String.format("INSUFFICIENT! (Has: %d, Missing: %d)", available, requestedContexts - available);
                    } else {
                        status = String.format("OK (Has: %d)", available);
                    }
                } else {
                    available = questionV2Repository.countSingleQuestionsAvailable(
                            getQuestionTopic(rule.getTopicName()).getId(),
                            getQuestionDifficulty(rule.getDifficultyName()).getId(),
                            type,
                            currentUser.getId()
                    );
                    requirementDesc = String.format("Need %d Single Questions", requestedQty);

                    if (available < requestedQty) {
                        status = String.format("INSUFFICIENT! (Has: %d, Missing: %d)", available, requestedQty - available);
                    } else {
                        status = String.format("OK (Has: %d)", available);
                    }
                }

                rulesAnalysisReport.append(String.format("- Rule #%d [%s - %s - %s]: %s -> %s\n",
                        ruleIndex++, topicName, diffName, type, requirementDesc, status));
            }
        } else {
            rulesAnalysisReport.append("No rules defined in this template.\n");
        }

        String userContext = String.format("""
            === EXAM TEMPLATE ANALYSIS REQUEST ===
            
            [1] EXAM METADATA
            - Title: %s
            - Subject: %s
            - Duration: %d minutes
            - Passing Score: %d
            - Token Cost: %s
            
            [2] QUESTION BANK INVENTORY CHECK
            %s
            
            Please analyze the feasibility of creating this exam based on the inventory check above.
            """,
                request.getTitle(),
                subjectName,
                request.getDuration(),
                request.getPassingScore(),
                request.getTokenCost() != null ? request.getTokenCost() : 0,
                rulesAnalysisReport.toString()
        );

        String systemPrompt = """
            You are an AI Exam Assistant for a teacher.
            Based on the provided "EXAM TEMPLATE ANALYSIS REQUEST", generate a friendly but professional summary response in English.
            
            Structure of response:
            1. **Overview**: Summarize the exam structure (Title, Subject, Time...).
            2. **Inventory Check**: Detailed analysis of whether the question bank has enough questions for each rule based on the data provided.
            3. **Recommendation**:
               - If strictly NOT enough questions: Warn the teacher clearly and suggest importing more specific topics/types.
               - If enough but barely (e.g., require 10, have 11): Suggest adding more for better randomization.
               - If plenty: Confirm the exam will be high quality.
            
            Output as plain text or Markdown. Do not include JSON formatting.

            """;

        return primaryChatClientWithoutMemory.prompt()
                .system(systemPrompt)
                .user(userContext)
                .call()
                .content();
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
