package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.FlashcardItemRequest;
import com.fa25se225.capstone.dto.request.FlashcardSetRequest;
import com.fa25se225.capstone.dto.response.FlashcardSetDetailResponse;
import com.fa25se225.capstone.dto.response.FlashcardSetResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.response.PostResponse;
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

import java.util.ArrayList;
import java.util.List;

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
                .isPublic(request.isPublic())
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

        if (!set.isPublic()) {
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

        User user = accountUtil.getCurrentUser();
        Pageable pageable = pageHelper.pageEngine(page, size, "viewCount:desc", "createdAt:desc");
        Page<FlashcardSet> fsPage = flashcardSetRepository.searchSets(keyword, user.getId(), pageable);
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
        set.setPublic(request.isPublic());

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
}