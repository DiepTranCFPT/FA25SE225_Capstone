package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.PostCreationRequest;
import com.fa25se225.capstone.dto.request.PostUpdateRequest;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.forum.Comment;
import com.fa25se225.capstone.entity.forum.Community;
import com.fa25se225.capstone.entity.forum.Post;
import com.fa25se225.capstone.entity.forum.PostVote;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.PostMapper;
import com.fa25se225.capstone.repository.CommunityRepository;
import com.fa25se225.capstone.repository.PostRepository;
import com.fa25se225.capstone.repository.PostVoteRepository;
import com.fa25se225.capstone.service.PostService;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final CloudinaryService cloudinaryService;
    private final AccountUtil accountUtil;
    private final PostMapper postMapper;
    private final PageHelper pageHelper;
    private final PostVoteRepository postVoteRepository;
    private final static String COMMUNITY_POSTS_FOLDER = "community_posts";

    @Override
    @Transactional
    public void votePost(String postId, int value) {
        User currentUser = accountUtil.getCurrentUser();
        Post post = findPostByIdOrThrowException(postId);

        if (value != 1 && value != -1 && value != 0) {
            throw new AppException(ErrorCode.INVALID_VOTE_VALUE);
        }

        Optional<PostVote> existingVoteOpt = postVoteRepository.findByUserAndPost(currentUser, post);
        int currentVoteCount = post.getVoteCount();

        if (existingVoteOpt.isPresent()) {
            PostVote existingVote = existingVoteOpt.get();
            int oldValue = existingVote.getValue();

            if (value == 0) {
                postVoteRepository.delete(existingVote);
                post.setVoteCount(currentVoteCount - oldValue);
            } else if (oldValue != value) {
                existingVote.setValue(value);
                postVoteRepository.save(existingVote);
                post.setVoteCount(currentVoteCount - oldValue + value);
            }
        } else {
            if (value != 0) {
                PostVote newVote = PostVote.builder()
                        .user(currentUser)
                        .post(post)
                        .value(value)
                        .build();
                postVoteRepository.save(newVote);
                post.setVoteCount(currentVoteCount + value);
            }
        }

        postRepository.save(post);
    }

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

    @Override
    public PageResponse<List<PostResponse>> getPostsByCommunityId(String communityId, int page, int size) {
        if (!communityRepository.existsById(communityId)) {
            throw new AppException(ErrorCode.COMMUNITY_NOT_FOUND);
        }

        Pageable pageable = pageHelper.pageEngine(page, size, "isPinned:desc", "createdAt:desc");
        Page<Post> postsPage = postRepository.findAllByCommunityId(communityId, pageable);
        List<Post> posts = postsPage.getContent();

        User currentUser = null;
        try {
            currentUser = accountUtil.getCurrentUser();
        } catch (Exception e) {
        }

        Map<String, Integer> userVotesMap = new HashMap<>();
        if (currentUser != null && !posts.isEmpty()) {
            List<String> postIds = posts.stream().map(Post::getId).toList();
            List<PostVote> votes = postVoteRepository.findAllByUserIdAndPostIds(currentUser.getId(), postIds);

            for (PostVote v : votes) {
                userVotesMap.put(v.getPost().getId(), v.getValue());
            }
        }

        List<PostResponse> responseItems = posts.stream().map(post -> {
            PostResponse res = postMapper.toResponse(post);
            res.setUserVoteValue(userVotesMap.getOrDefault(post.getId(), 0));
            return res;
        }).toList();

        return PageResponse.<List<PostResponse>>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPage(postsPage.getTotalPages())
                .totalElement(postsPage.getTotalElements())
                .items(responseItems)
                .build();
    }

    private Post findPostByIdOrThrowException(String postId){
        return postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }
}
