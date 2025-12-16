package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.LessonVideoProgress;
import com.fa25se225.capstone.entity.Lesson;
import com.fa25se225.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LessonVideoProgressRepository extends JpaRepository<LessonVideoProgress, Long> {
    Optional<LessonVideoProgress> findByUserAndLesson(User user, Lesson lesson);
}

