package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.FlashcardSetRequest;
import com.fa25se225.capstone.dto.response.FlashcardSetDetailResponse;
import com.fa25se225.capstone.dto.response.FlashcardSetResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface FlashcardService {
    FlashcardSetDetailResponse createFlashcardSet(FlashcardSetRequest request);
    FlashcardSetDetailResponse getFlashcardSetDetail(String id);

    PageResponse<List<FlashcardSetResponse>> searchSets(String keyword, int page, int size);
    void deleteFlashcardSet(String id);

    FlashcardSetDetailResponse updateFlashcardSet(String id, FlashcardSetRequest request);
}
