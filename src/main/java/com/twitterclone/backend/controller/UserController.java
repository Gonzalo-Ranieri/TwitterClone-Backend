package com.twitterclone.backend.controller;

import com.twitterclone.backend.model.User;
import com.twitterclone.backend.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final FollowService followService;

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followUser(
            @PathVariable("id") UUID targetUserId,
            @AuthenticationPrincipal User currentUser
    ) {
        followService.followUser(targetUserId, currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable("id") UUID targetUserId,
            @AuthenticationPrincipal User currentUser
    ) {
        followService.unfollowUser(targetUserId, currentUser);
        return ResponseEntity.ok().build();
    }
}
