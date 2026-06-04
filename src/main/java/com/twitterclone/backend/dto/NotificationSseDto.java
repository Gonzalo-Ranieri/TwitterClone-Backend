package com.twitterclone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSseDto {
    private UUID id;
    private String actorUsername;
    private String type;
    private UUID referenceId;
    private LocalDateTime createdAt;
}
