package com.twitterclone.backend.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String username;
    private String email;
    private String bio;
    private String avatarPlaceholder;
    private String bannerPlaceholder;
    private boolean showEmail;
    private long followersCount;
    private long followingCount;
    private boolean followedByCurrentUser;
}
