package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.forum.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author LEFT JOIN FETCH c.replyToUser WHERE c.post.id = :postId AND c.parent IS NULL")
    Page<Comment> findRootCommentsByPostId(@Param("postId") String postId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author LEFT JOIN FETCH c.replyToUser WHERE c.parent.id = :parentId")
    Page<Comment> findRepliesByParentId(@Param("parentId") String parentId, Pageable pageable);
}