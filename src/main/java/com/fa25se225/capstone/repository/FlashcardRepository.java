package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.flashcard.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, String> {
}
