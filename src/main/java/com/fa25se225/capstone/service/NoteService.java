package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.NoteRequest;
import com.fa25se225.capstone.dto.request.UpdateNoteRequest;
import com.fa25se225.capstone.dto.response.NoteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoteService {
    NoteResponse createNote(NoteRequest request);
    NoteResponse updateNote(String noteId, UpdateNoteRequest request);
    void deleteNote(String noteId);
    Page<NoteResponse> getMyNotes(Pageable pageable);
    NoteResponse getNoteByUserAndLesson(String userId, String lessonId);
    Page<NoteResponse> getNotesByLesson(String lessonId, Pageable pageable);
}
