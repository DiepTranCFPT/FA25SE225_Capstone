package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.NoteResponse;
import com.fa25se225.capstone.entity.Note;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NoteMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(getUserFullName(note))")
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "lessonName", source = "lesson.name")
    NoteResponse toResponse(Note note);
    
    default String getUserFullName(Note note) {
        if (note.getUser() != null) {
            var user = note.getUser();
            return (user.getFirstName() != null ? user.getFirstName() : "") + " " + 
                   (user.getLastName() != null ? user.getLastName() : "");
        }
        return null;
    }
}
