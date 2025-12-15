package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.forum.Post;
import com.fa25se225.capstone.entity.forum.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, String> {
    Optional<PostVote> findByUserAndPost(User user, Post post);

    // Hàm này dùng để lấy trạng thái vote của user cho 1 danh sách bài viết (tối ưu hiệu năng)
    @Query("SELECT pv FROM PostVote pv WHERE pv.user.id = :userId AND pv.post.id IN :postIds")
    List<PostVote> findAllByUserIdAndPostIds(@Param("userId") String userId, @Param("postIds") List<String> postIds);
}