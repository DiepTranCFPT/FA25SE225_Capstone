package com.fa25se225.capstone.entity.forum;

import com.fa25se225.capstone.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private int value; // 1: Upvote, -1: Downvote
}