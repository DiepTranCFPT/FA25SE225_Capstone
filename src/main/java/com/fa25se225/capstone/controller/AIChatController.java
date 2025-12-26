package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.AIGenerateQuestionRequest;
import com.fa25se225.capstone.dto.request.ExamAskingRequest;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.service.implementation.AIChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("ai")
public class AIChatController {
    private final AIChatService aiChatService;


    @PostMapping("/exam-ask")
    public SseEmitter chat(@RequestBody ExamAskingRequest request) {
        SseEmitter sseEmitter = new SseEmitter(-1L);
        aiChatService.examAsk(request)
                .subscribe(
                        token -> {
                            log.info("SSe Received token");
                            try {
                                sseEmitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            sseEmitter.completeWithError(error);
                        },
                        () -> {
                            log.info("SSe Stream completed");
                            sseEmitter.complete();
                        }
                );

        return sseEmitter;
    }

    @PostMapping("/analyze-template")
    public String analyzeTemplate(@RequestBody ExamTemplateV2Request request) {
        return aiChatService.analyzeTemplateFeasibility(request);
    }

    @PostMapping("/generate-questions/{subjectId}")
    public List<QuestionCreationV2Request> generateQuestions(@PathVariable String subjectId, @Valid @RequestBody AIGenerateQuestionRequest request) {
        return aiChatService.parseRawTextToQuestionJson(subjectId, request);
    }

    @PostMapping("/students/dashboard")
    public SseEmitter chatStudentDashBoard(@RequestBody String prompt) {
        SseEmitter sseEmitter = new SseEmitter(-1L);
        aiChatService.chatStudentDashBoard(prompt)
                .subscribe(
                        token -> {
                            log.info("SSe Received token");
                            try {
                                sseEmitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            sseEmitter.completeWithError(error);
                        },
                        () -> {
                            log.info("SSe Stream completed");
                            sseEmitter.complete();
                        }
                );

        return sseEmitter;
    }

}
