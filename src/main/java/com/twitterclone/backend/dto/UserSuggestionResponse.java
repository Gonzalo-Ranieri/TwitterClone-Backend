package com.twitterclone.backend.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuggestionResponse {
    private UUID id;
    private String username;
    private String bio;
    private String avatarPlaceholder;
}
