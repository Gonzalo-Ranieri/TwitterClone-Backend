package com.twitterclone.backend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TweetResponse {
    private UUID id;
    private String content;
    private UUID authorId;
    private String authorUsername;
    private String authorAvatarPlaceholder;
    private LocalDateTime createdAt;
    private long likeCount;
    private boolean liked;
    private int replyCount;
    private UUID parentTweetId;

    // Constructor for JPQL projection mapping Count as Long and liked as Boolean
    public TweetResponse(UUID id, String content, UUID authorId, String authorUsername, String authorAvatarPlaceholder, LocalDateTime createdAt, Long likeCount, Boolean liked, Integer replyCount, UUID parentTweetId) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorAvatarPlaceholder = authorAvatarPlaceholder;
        this.createdAt = createdAt;
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.liked = liked != null ? liked : false;
        this.replyCount = replyCount != null ? replyCount : 0;
        this.parentTweetId = parentTweetId;
    }
}
