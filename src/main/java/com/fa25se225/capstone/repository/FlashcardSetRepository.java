package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.flashcard.FlashcardSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, String> {

    @Query("SELECT fs FROM FlashcardSet fs WHERE " +
            "(fs.title LIKE %:keyword% OR fs.description LIKE %:keyword%) " +
            "AND (fs.isPublic = true OR fs.author.id = :userId) " +
            "ORDER BY fs.createdAt DESC")
    Page<FlashcardSet> searchSets(@Param("keyword") String keyword,
                                  @Param("userId") String userId,
                                  Pageable pageable);

    @Query("SELECT fs FROM FlashcardSet fs LEFT JOIN FETCH fs.flashcards WHERE fs.id = :id")
    Optional<FlashcardSet> findByIdWithCards(@Param("id") String id);
}