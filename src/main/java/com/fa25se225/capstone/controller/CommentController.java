package com.fa25se225.capstone.controller;


import com.fa25se225.capstone.dto.request.CommentRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.CommentResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // 2. Lấy comment cấp 2 (Replies) của một comment cha
    @GetMapping("/{commentId}/replies")
    public ApiResponse<PageResponse<CommentResponse>> getReplies(
            @PathVariable String commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(commentService.getReplies(commentId, page, size));
    }

    // 3. Tạo comment (Post hoặc Reply đều dùng cái này)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommentResponse> createComment(@ModelAttribute CommentRequest request) {
        return ApiResponse.success(commentService.createComment(request));
    }
}
