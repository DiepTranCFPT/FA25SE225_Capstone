package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.FlashcardResponse;
import com.fa25se225.capstone.entity.flashcard.Flashcard;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlashCardMapper {
    FlashcardResponse toResponse(Flashcard flashcard);
}
