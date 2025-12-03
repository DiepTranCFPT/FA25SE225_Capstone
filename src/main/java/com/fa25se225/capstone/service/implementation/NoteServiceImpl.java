package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.NoteRequest;
import com.fa25se225.capstone.dto.request.UpdateNoteRequest;
import com.fa25se225.capstone.dto.response.NoteResponse;
import com.fa25se225.capstone.entity.Lesson;
import com.fa25se225.capstone.entity.Note;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.NoteMapper;
import com.fa25se225.capstone.repository.LessonRepository;
import com.fa25se225.capstone.repository.NoteRepository;
import com.fa25se225.capstone.service.NoteService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    
    private final NoteRepository noteRepository;
    private final LessonRepository lessonRepository;
    private final NoteMapper noteMapper;
    private final AccountUtil accountUtil;

    @Override
    @Transactional
    public NoteResponse createNote(NoteRequest request) {
        User currentUser = accountUtil.getCurrentUser();
        
        Lesson lesson = lessonRepository.findById(request.getLessonId())
            .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        
        if (noteRepository.existsByUserIdAndLessonIdAndDeletedFalse(currentUser.getId(), request.getLessonId())) {
            throw new AppException(ErrorCode.NOTE_ALREADY_EXISTS);
        }
        
        Note note = Note.builder()
            .user(currentUser)
            .lesson(lesson)
            .description(request.getDescription())
            .build();
        
        note = noteRepository.save(note);
        
        log.info("User {} created note for lesson {}", currentUser.getId(), lesson.getId());
        
        return noteMapper.toResponse(note);
    }

    @Override
    @Transactional
    public NoteResponse updateNote(String noteId, UpdateNoteRequest request) {
        User currentUser = accountUtil.getCurrentUser();
        
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new AppException(ErrorCode.NOTE_NOT_FOUND));
        
        if (!note.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        if (note.getDeleted()) {
            throw new AppException(ErrorCode.NOTE_NOT_FOUND);
        }
        
        note.setDescription(request.getDescription());
        note = noteRepository.save(note);
        
        log.info("User {} updated note {}", currentUser.getId(), noteId);
        
        return noteMapper.toResponse(note);
    }

    @Override
    @Transactional
    public void deleteNote(String noteId) {
        User currentUser = accountUtil.getCurrentUser();
        
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new AppException(ErrorCode.NOTE_NOT_FOUND));
        
        if (!note.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        if (note.getDeleted()) {
            throw new AppException(ErrorCode.NOTE_NOT_FOUND);
        }
        
        note.setDeleted(true);
        noteRepository.save(note);
        
        log.info("User {} deleted note {}", currentUser.getId(), noteId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> getMyNotes(Pageable pageable) {
        User currentUser = accountUtil.getCurrentUser();
        
        Page<Note> notes = noteRepository.findByUserIdAndDeletedFalse(currentUser.getId(), pageable);
        return notes.map(noteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse getNoteByUserAndLesson(String userId, String lessonId) {
        Note note = noteRepository.findByUserIdAndLessonIdAndDeletedFalse(userId, lessonId)
            .orElseThrow(() -> new AppException(ErrorCode.NOTE_NOT_FOUND));
        
        return noteMapper.toResponse(note);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> getNotesByLesson(String lessonId, Pageable pageable) {
        Page<Note> notes = noteRepository.findByLessonIdAndDeletedFalse(lessonId, pageable);
        return notes.map(noteMapper::toResponse);
    }
}
