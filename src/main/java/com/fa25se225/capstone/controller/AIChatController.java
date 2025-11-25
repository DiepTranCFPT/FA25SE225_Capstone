package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.ExamAskingRequest;
import com.fa25se225.capstone.service.implementation.AIChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("ai")
public class AIChatController {
    private final AIChatService aiChatService;

    @PostMapping("/exam-ask")
    public String examAsk(@Valid @RequestBody ExamAskingRequest request){
        return aiChatService.examAsk(request);
    }

}
