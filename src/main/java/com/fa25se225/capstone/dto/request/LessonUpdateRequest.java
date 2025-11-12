package com.fa25se225.capstone.dto.request;

public record LessonUpdateRequest(
    String name,

    String file,

    String url,

    String questionId,

    String learningMaterialId
) {}
