package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.NoteRequest;
import com.fa25se225.capstone.dto.request.UpdateNoteRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.NoteResponse;
import com.fa25se225.capstone.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor

public class NoteController {
    
    private final NoteService noteService;

    @PostMapping
    public ApiResponse<NoteResponse> createNote(@Valid @RequestBody NoteRequest request) {
        log.info("Creating note for lesson: {}", request.getLessonId());
        NoteResponse response = noteService.createNote(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{noteId}")
    public ApiResponse<NoteResponse> updateNote(
            @PathVariable String noteId,
            @Valid @RequestBody UpdateNoteRequest request) {
        log.info("Updating note: {}", noteId);
        NoteResponse response = noteService.updateNote(noteId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{noteId}")
    public ApiResponse<Void> deleteNote(@PathVariable String noteId) {
        log.info("Deleting note: {}", noteId);
        noteService.deleteNote(noteId);
        return ApiResponse.success(null);
    }

    @GetMapping("/my-notes")
    public ApiResponse<Page<NoteResponse>> getMyNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NoteResponse> notes = noteService.getMyNotes(pageable);
        return ApiResponse.success(notes);
    }

    @GetMapping("/lesson/{lessonId}/user/{userId}")
    public ApiResponse<NoteResponse> getNoteByUserAndLesson(
            @PathVariable String lessonId,
            @PathVariable String userId) {
        
        NoteResponse note = noteService.getNoteByUserAndLesson(userId, lessonId);
        return ApiResponse.success(note);
    }

    @GetMapping("/lesson/{lessonId}")
    public ApiResponse<Page<NoteResponse>> getNotesByLesson(
            @PathVariable String lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NoteResponse> notes = noteService.getNotesByLesson(lessonId, pageable);
        return ApiResponse.success(notes);
    }
}
