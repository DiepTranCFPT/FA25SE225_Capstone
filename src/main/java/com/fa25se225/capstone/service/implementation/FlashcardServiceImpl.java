package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.FlashcardItemRequest;
import com.fa25se225.capstone.dto.request.FlashcardSetRequest;
import com.fa25se225.capstone.dto.response.FlashcardSetDetailResponse;
import com.fa25se225.capstone.dto.response.FlashcardSetResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.response.QuizQuestionResponse;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.flashcard.Flashcard;
import com.fa25se225.capstone.entity.flashcard.FlashcardSet;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.FlashCardSetMapper;
import com.fa25se225.capstone.repository.FlashcardSetRepository;
import com.fa25se225.capstone.service.FlashcardService;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final AccountUtil accountUtil;
    private final FlashCardSetMapper flashCardSetMapper;
    private final PageHelper pageHelper;

    @Override
    @Transactional
    public FlashcardSetDetailResponse createFlashcardSet(FlashcardSetRequest request) {
        User currentUser = accountUtil.getCurrentUser();

        FlashcardSet set = FlashcardSet.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .visible(request.isPublic())
                .author(currentUser)
                .cardCount(request.getCards().size())
                .viewCount(0)
                .build();

        List<Flashcard> cards = new ArrayList<>();
        for (int i = 0; i < request.getCards().size(); i++) {
            FlashcardItemRequest item = request.getCards().get(i);
            cards.add(Flashcard.builder()
                    .term(item.getTerm())
                    .definition(item.getDefinition())
                    .imageUrl(item.getImageUrl())
                    .displayOrder(i + 1)
                    .flashcardSet(set)
                    .build());
        }
        set.setFlashcards(cards);

        FlashcardSet savedSet = flashcardSetRepository.save(set);
        return flashCardSetMapper.toDetailResponse(savedSet);
    }

    @Override
    public FlashcardSetDetailResponse getFlashcardSetDetail(String id) {
        FlashcardSet set = flashcardSetRepository.findByIdWithCards(id)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_SET_NOT_FOUND));

        User currentUser = null;
        try { currentUser = accountUtil.getCurrentUser(); }
        catch (Exception e) {

        }

        if (!set.isVisible()) {
            if (currentUser == null || !set.getAuthor().getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        set.setViewCount(set.getViewCount() + 1);
        flashcardSetRepository.save(set);

        return flashCardSetMapper.toDetailResponse(set);
    }

    @Override
    public PageResponse<List<FlashcardSetResponse>> searchSets(String keyword, int page, int size) {

        User currentUser = null;
        try { currentUser = accountUtil.getCurrentUser(); }
        catch (Exception e) {
        }
        String currentId = Objects.isNull(currentUser) ? "1" : currentUser.getId();
        Pageable pageable = pageHelper.pageEngine(page, size, "viewCount:desc", "createdAt:desc");
        Page<FlashcardSet> fsPage = flashcardSetRepository.searchSets(keyword, currentId, pageable);
        List<FlashcardSetResponse> responseItems = fsPage.getContent().stream().map(flashCardSetMapper::toResponse).toList();
        return PageResponse.<List<FlashcardSetResponse>>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPage(fsPage.getTotalPages())
                .totalElement(fsPage.getTotalElements())
                .items(responseItems)
                .build();
    }

    @Override
    @Transactional
    public FlashcardSetDetailResponse updateFlashcardSet(String id, FlashcardSetRequest request) {
        FlashcardSet set = flashcardSetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_SET_NOT_FOUND));

        User currentUser = accountUtil.getCurrentUser();
        if (!set.getAuthor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        set.setTitle(request.getTitle());
        set.setDescription(request.getDescription());
        set.setVisible(request.isPublic());

        set.getFlashcards().clear();
        
        for (int i = 0; i < request.getCards().size(); i++) {
            FlashcardItemRequest item = request.getCards().get(i);
            set.getFlashcards().add(Flashcard.builder()
                    .term(item.getTerm())
                    .definition(item.getDefinition())
                    .imageUrl(item.getImageUrl())
                    .displayOrder(i + 1)
                    .flashcardSet(set)
                    .build());
        }
        set.setCardCount(set.getFlashcards().size());

        FlashcardSet savedSet = flashcardSetRepository.save(set);
        return flashCardSetMapper.toDetailResponse(savedSet);
    }

    @Override
    @Transactional
    public void deleteFlashcardSet(String id) {
        FlashcardSet set = flashcardSetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_SET_NOT_FOUND));

        User currentUser = accountUtil.getCurrentUser();
        if (!set.getAuthor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        flashcardSetRepository.delete(set);
    }

    @Override
    public List<QuizQuestionResponse> generateQuiz(String setId) {
        FlashcardSet set = flashcardSetRepository.findByIdWithCards(setId)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_SET_NOT_FOUND));

        List<Flashcard> cards = set.getFlashcards();
        if (cards.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> allTerms = cards.stream().map(Flashcard::getTerm).toList();

        List<QuizQuestionResponse> quiz = new ArrayList<>();

        for (Flashcard card : cards) {
            List<String> distractors = new ArrayList<>(allTerms);
            distractors.remove(card.getTerm());
            Collections.shuffle(distractors);

            List<String> options = new ArrayList<>(distractors.subList(0, Math.min(3, distractors.size())));
            
            options.add(card.getTerm());
            
            Collections.shuffle(options);

            quiz.add(QuizQuestionResponse.builder()
                    .flashcardId(card.getId())
                    .question(card.getDefinition())
                    .imageUrl(card.getImageUrl())
                    .correctAnswer(card.getTerm())
                    .options(options)
                    .build());
        }
        
        return quiz;
    }

    @Override
    public PageResponse<List<FlashcardSetResponse>> getMySets(int page, int size) {
        User user = accountUtil.getCurrentUser();
        Pageable pageable = pageHelper.pageEngine(page, size, "viewCount:desc", "createdAt:desc");
        Page<FlashcardSet> fsPage = flashcardSetRepository.getByAuthor(user, pageable);
        List<FlashcardSetResponse> responseItems = fsPage.getContent().stream().map(flashCardSetMapper::toResponse).toList();
        return PageResponse.<List<FlashcardSetResponse>>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPage(fsPage.getTotalPages())
                .totalElement(fsPage.getTotalElements())
                .items(responseItems)
                .build();
    }

    @Override
    @Transactional
    public void setVisibility(String id) {
        FlashcardSet set = flashcardSetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FLASHCARD_SET_NOT_FOUND));
        set.setVisible(!set.isVisible());
        flashcardSetRepository.save(set);
    }
}