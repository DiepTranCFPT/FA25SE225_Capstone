package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.PostCreationRequest;
import com.fa25se225.capstone.dto.request.PostUpdateRequest;
import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.forum.Comment;
import com.fa25se225.capstone.entity.forum.Community;
import com.fa25se225.capstone.entity.forum.Post;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.PostMapper;
import com.fa25se225.capstone.repository.CommunityRepository;
import com.fa25se225.capstone.repository.PostRepository;
import com.fa25se225.capstone.service.PostService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final CloudinaryService cloudinaryService;
    private final AccountUtil accountUtil;
    private final PostMapper postMapper;
    private final static String COMMUNITY_POSTS_FOLDER = "community_posts";

    @Override
    @Transactional
    public PostResponse createPost(String communityId, PostCreationRequest request) {
        User currentUser = accountUtil.getCurrentUser();

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

        String imgUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imgUrl = cloudinaryService.uploadFile(request.getImage(), COMMUNITY_POSTS_FOLDER);
        }

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(imgUrl)
                .author(currentUser)
                .community(community)
                .isPinned(false)
                .build();

        Post savedPost = postRepository.save(post);
        return postMapper.toResponse(savedPost);
    }

    @Override
    @Transactional
    public void togglePinPost(String postId) {
        Post post = findPostByIdOrThrowException(postId);

        post.setPinned(!post.isPinned());
        postRepository.save(post);
    }

    @Override
    public void updatePost(String id, PostUpdateRequest request) {
        Post post = findPostByIdOrThrowException(id);
        postMapper.updatePost(post, request);
        postRepository.save(post);

    }

    @Override
    public void deletePost(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        String imgUrl = post.getImageUrl();
        if(Objects.nonNull(imgUrl)) {
            String publicId = cloudinaryService.getPublicIdFromUrl(imgUrl);
            if (Strings.isNotEmpty(publicId)) {
                cloudinaryService.deleteFile(publicId);
            }
        }
        postRepository.delete(post);
    }

    private Post findPostByIdOrThrowException(String postId){
        return postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }
}
