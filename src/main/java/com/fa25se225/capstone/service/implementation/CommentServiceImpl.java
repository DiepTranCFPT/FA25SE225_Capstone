package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.CommentRequest;
import com.fa25se225.capstone.dto.response.CommentResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.forum.Comment;
import com.fa25se225.capstone.entity.forum.Post;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.CommentMapper;
import com.fa25se225.capstone.repository.CommentRepository;
import com.fa25se225.capstone.repository.PostRepository;
import com.fa25se225.capstone.service.CommentService;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AccountUtil accountUtil;
    private final CloudinaryService cloudinaryService;
    private final CommentMapper commentMapper;
    private final PageHelper pageHelper;
    private static final String COMMENT_IMAGE_FOLDER = "comment_images";


    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        User currentUser = accountUtil.getCurrentUser();

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        Comment parentComment = null;
        User replyToUser = null;

        if (Objects.nonNull(request.getParenCommentId())) {
            parentComment = commentRepository.findById(request.getParenCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            if (!Objects.equals(parentComment.getPost().getId(), post.getId())) {
                throw new AppException(ErrorCode.COMMENT_NOT_SAME_THE_POST);
            }

            replyToUser = parentComment.getAuthor();

            // normalize to root so replies are max 2 levels
            if (parentComment.getParent() != null) {
                parentComment = parentComment.getParent();
            }

            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            commentRepository.save(parentComment);
        }

        String imgUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imgUrl = cloudinaryService.uploadFile(request.getImage(), COMMENT_IMAGE_FOLDER);
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .imgUrl(imgUrl)
                .post(post)
                .author(currentUser)
                .parent(parentComment)
                .replyToUser(replyToUser)
                .replyCount(0)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    @Override
    public PageResponse<List<CommentResponse>> getCommentsByPostId(String postId, int page, int size) {
        Pageable pageable = pageHelper.pageEngine(page, size, "likeCount:desc", "createdAt:desc");

        Page<Comment> commentPage = commentRepository.findRootCommentsByPostId(postId, pageable);

        return buildPageResponse(commentPage, page, size);
    }

    @Override
    public PageResponse<List<CommentResponse>> getReplies(String commentId, int page, int size) {
        Pageable pageable = pageHelper.pageEngine(page, size, "createdAt:asc");

        Page<Comment> replyPage = commentRepository.findRepliesByParentId(commentId, pageable);

        return buildPageResponse(replyPage, page, size);
    }

    @Override
    public void deleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        String imgUrl = comment.getImgUrl();
        if(Objects.nonNull(imgUrl)) {
            String publicId = cloudinaryService.getPublicIdFromUrl(imgUrl);
            if (Strings.isNotEmpty(publicId)) {
                cloudinaryService.deleteFile(publicId);
            }
        }
        commentRepository.delete(comment);
    }

    @Override
    public void updateComment(String id, String content) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        comment.setContent(content);
        commentRepository.save(comment);

    }


    private PageResponse<List<CommentResponse>>buildPageResponse(Page<Comment> pageData, int page, int size) {
        List<CommentResponse> items = pageData.getContent().stream()
                .map(commentMapper::toResponse)
                .toList();

        return PageResponse.<List<CommentResponse>>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPage(pageData.getTotalPages())
                .totalElement(pageData.getTotalElements())
                .items(items)
                .build();
    }
}
