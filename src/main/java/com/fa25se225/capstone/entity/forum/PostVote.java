package com.fa25se225.capstone.entity.forum;

import com.fa25se225.capstone.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "post_votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
        //Each person can only vote once for each post
})
public class PostVote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Post post;

    private int value;
    // 1 (Upvote), -1 (Downvote), 0 (Unvote)
}