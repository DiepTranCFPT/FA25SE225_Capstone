package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {

    Optional<Note> findByUserIdAndLessonIdAndDeletedFalse(String userId, String lessonId);

    Page<Note> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    Page<Note> findByLessonIdAndDeletedFalse(String lessonId, Pageable pageable);

    boolean existsByUserIdAndLessonIdAndDeletedFalse(String userId, String lessonId);
}
