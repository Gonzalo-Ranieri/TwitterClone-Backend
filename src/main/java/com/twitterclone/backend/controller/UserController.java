package com.twitterclone.backend.controller;

import com.twitterclone.backend.model.User;
import com.twitterclone.backend.service.FollowService;
import com.twitterclone.backend.dto.FollowUserResponse;
import com.twitterclone.backend.dto.UserProfileResponse;
import com.twitterclone.backend.dto.UserSuggestionResponse;
import com.twitterclone.backend.repository.FollowRepository;
import com.twitterclone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final FollowService followService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        long followingCount = followRepository.countByFollowerId(user.getId());
        long followersCount = followRepository.countByFollowingId(user.getId());
        boolean followedByCurrentUser = followRepository.existsByFollowerAndFollowing(currentUser, user);

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarPlaceholder(user.getAvatarPlaceholder())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .followedByCurrentUser(followedByCurrentUser)
                .build());
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<Page<FollowUserResponse>> getFollowers(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        Page<FollowUserResponse> followers = followRepository.findFollowers(id, currentUser.getId(), pageable);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<Page<FollowUserResponse>> getFollowing(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        Page<FollowUserResponse> following = followRepository.findFollowing(id, currentUser.getId(), pageable);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FollowUserResponse>> searchUsers(
            @RequestParam("q") String query,
            @AuthenticationPrincipal User currentUser
    ) {
        List<FollowUserResponse> results = userRepository.searchUsers(query, currentUser.getId());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<UserSuggestionResponse>> getSuggestions(
            @AuthenticationPrincipal User currentUser
    ) {
        List<User> suggestions = userRepository.findFollowSuggestions(
                currentUser.getId(),
                PageRequest.of(0, 5)
        );

        List<UserSuggestionResponse> response = suggestions.stream()
                .map(u -> UserSuggestionResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .bio(u.getBio())
                        .avatarPlaceholder(u.getAvatarPlaceholder())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

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
