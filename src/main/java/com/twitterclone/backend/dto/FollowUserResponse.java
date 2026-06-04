package com.twitterclone.backend.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUserResponse {
    private UUID id;
    private String username;
    private String bio;
    private String avatarPlaceholder;
    private boolean followedByCurrentUser;

    // Constructor matching JPQL select fields mapping Boolean properly
    public FollowUserResponse(UUID id, String username, String bio, String avatarPlaceholder, Boolean followedByCurrentUser) {
        this.id = id;
        this.username = username;
        this.bio = bio;
        this.avatarPlaceholder = avatarPlaceholder;
        this.followedByCurrentUser = followedByCurrentUser != null ? followedByCurrentUser : false;
    }
}
