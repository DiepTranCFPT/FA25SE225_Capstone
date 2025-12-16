package com.fa25se225.capstone.mapper;


import com.fa25se225.capstone.dto.response.FlashcardSetDetailResponse;
import com.fa25se225.capstone.dto.response.FlashcardSetResponse;
import com.fa25se225.capstone.entity.flashcard.FlashcardSet;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {FlashCardMapper.class, UserMapper.class} )
public interface FlashCardSetMapper {
    FlashcardSetResponse toResponse(FlashcardSet flashcardSet);
    FlashcardSetDetailResponse toDetailResponse(FlashcardSet flashcardSet);
}
