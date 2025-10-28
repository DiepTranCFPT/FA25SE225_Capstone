package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.service.GeminiService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeminiChatController {

    private final GeminiService geminiService;

    public GeminiChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/chat")
    public String chat(@RequestParam String message) {
        return geminiService.chat(message);
    }
}
