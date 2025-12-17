package com.fa25se225.capstone.controller;


import com.fa25se225.capstone.dto.request.CommentRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.CommentResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}/replies")
    public ApiResponse<PageResponse<List<CommentResponse>>> getReplies(
            @PathVariable String commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(commentService.getReplies(commentId, page, size));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<String> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.success("Delete comment successfully");
    }

    @PutMapping("/{commentId}")
    public ApiResponse<String> updateComment(@PathVariable String commentId, @RequestBody String content) {
        commentService.updateComment(commentId, content);
        return ApiResponse.success("Update comment successfully");
    }

    @PostMapping("/{commentId}/vote")
    public ApiResponse<Void> voteComment(@PathVariable String commentId, @RequestParam int value) {
        commentService.voteComment(commentId, value);
        return ApiResponse.success(null);
    }
}
