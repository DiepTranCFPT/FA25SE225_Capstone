package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.forum.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    Page<Post> findAllByCommunityId(String communityId, Pageable pageable);
    Page<Post> findAllByAuthor(User author, Pageable pageable);
}
