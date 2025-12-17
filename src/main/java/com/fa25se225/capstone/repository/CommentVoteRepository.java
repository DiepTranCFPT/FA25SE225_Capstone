package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.forum.Comment;
import com.fa25se225.capstone.entity.forum.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, String> {
    Optional<CommentVote> findByUserAndComment(User user, Comment comment);

    @Query("SELECT cv FROM CommentVote cv WHERE cv.user.id = :userId AND cv.comment.id IN :commentIds")
    List<CommentVote> findAllByUserIdAndCommentIds(@Param("userId") String userId, @Param("commentIds") List<String> commentIds);
}