package com.fa25se225.capstone.service.implementation;



import com.fa25se225.capstone.dto.SubjectLessonCountProjection;
import com.fa25se225.capstone.dto.SubjectProgressDto;
import com.fa25se225.capstone.repository.ParentProfileRepository;
import com.fa25se225.capstone.service.ParentProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParentProgressServiceImpl implements ParentProgressService {

    private final ParentProfileRepository parentProfileRepository;

    @Override
    public List<SubjectProgressDto> getChildSubjectProgress(String parentId, String studentId) {

        if (!parentProfileRepository.isParentOfStudent(parentId, studentId)) {
            throw new AccessDeniedException("You are not allowed to view this student's progress");
        }

        List<SubjectLessonCountProjection> rows =
                parentProfileRepository.getSubjectProgressForStudent(studentId);

        return rows.stream().map(r -> {
            long total = r.getTotalLessons() == null ? 0L : r.getTotalLessons();
            long completed = r.getCompletedLessons() == null ? 0L : r.getCompletedLessons();
            boolean isCompleted = total > 0 && completed == total;

            return SubjectProgressDto.builder()
                    .subjectId(r.getSubjectId())
                    .subjectName(r.getSubjectName())
                    .totalLessons(total)
                    .completedLessons(completed)
                    .completed(isCompleted)
                    .build();
        }).toList();
    }

    @Override
    public List<SubjectProgressDto> getChildCompletedSubjects(String parentId, String studentId) {
        if (!parentProfileRepository.isParentOfStudent(parentId, studentId)) {
            throw new AccessDeniedException("You are not allowed to view this student's progress");
        }

        return parentProfileRepository.getCompletedSubjectsForStudent(studentId)
                .stream()
                .map(r -> SubjectProgressDto.builder()
                        .subjectId(r.getSubjectId())
                        .subjectName(r.getSubjectName())
                        .totalLessons(r.getTotalLessons() == null ? 0L : r.getTotalLessons())
                        .completedLessons(r.getCompletedLessons() == null ? 0L : r.getCompletedLessons())
                        .completed(true)
                        .build()
                )
                .toList();
    }
}
