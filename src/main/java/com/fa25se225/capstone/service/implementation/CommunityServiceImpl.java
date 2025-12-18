package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.CommunityCreationRequest;
import com.fa25se225.capstone.dto.request.CommunityUpdateRequest;
import com.fa25se225.capstone.dto.response.CommunityResponse;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.forum.Comment;
import com.fa25se225.capstone.entity.forum.Community;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.CommentMapper;
import com.fa25se225.capstone.mapper.CommunityMapper;
import com.fa25se225.capstone.repository.CommunityRepository;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.service.CommunityService;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMapper communityMapper;
    private final CloudinaryService cloudinaryService;
    private final SubjectRepository subjectRepository;
    private static final String COMMUNITY_IMAGE_FOLDER = "communities_image";


    @Override
    public List<CommunityResponse> getAll() {
        return communityRepository.findAll().stream().sorted(Comparator.comparing(Community::getCreateAt)).map(communityMapper::toResponse).toList();
    }


    @Override
    public CommunityResponse createCommunity(CommunityCreationRequest request) {
        String imgUrl = null;
        if (Objects.nonNull(request.getImage())) {
            imgUrl = cloudinaryService.uploadFile(request.getImage(), COMMUNITY_IMAGE_FOLDER);
        }
        Subject subject = null;
        if(Strings.isNotBlank(request.getSubjectId())) {
            subject = subjectRepository.findById(request.getSubjectId()).get();
        }
        Community community = Community.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imgUrl(imgUrl)
                .subject(subject)
                .build();
        var rsp = communityRepository.save(community);
        return communityMapper.toResponse(rsp);
    }

    @Override
    public void deleteCommunity(String communityId) {
        communityRepository.deleteById(communityId);
    }

    @Override
    public void updateCommunity(String communityId, CommunityUpdateRequest request) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));
        if(Strings.isNotBlank(request.getName())) {
            community.setName(request.getName());
        }
        if(Strings.isNotBlank(request.getDescription())) {
            community.setDescription(request.getDescription());
        }
        String imgUrl = null;
        if (Objects.nonNull(request.getImage())) {
            imgUrl = cloudinaryService.uploadFile(request.getImage(), COMMUNITY_IMAGE_FOLDER);
        }
        community.setImgUrl(imgUrl);

        communityRepository.save(community);
    }

    @Override
    public List<CommunityResponse> searchCommunity(String keyword) {
        List<Community> communities = communityRepository.findByNameContainingIgnoreCase(keyword);
        return communities.stream()
                .map(communityMapper::toResponse)
                .toList();
    }


}
