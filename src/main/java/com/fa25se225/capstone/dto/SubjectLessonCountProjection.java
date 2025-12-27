package com.fa25se225.capstone.dto;

public interface SubjectLessonCountProjection {
    String getSubjectId();
    String getSubjectName();
    String getLearningName();
    String LessonName();
    Long getTotalLessons();
    Long getCompletedLessons();
}

