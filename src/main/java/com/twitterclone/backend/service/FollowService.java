package com.twitterclone.backend.service;

import com.twitterclone.backend.model.Follow;
import com.twitterclone.backend.model.NotificationType;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.FollowRepository;
import com.twitterclone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void followUser(UUID targetUserId, User currentUser) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes seguirte a ti mismo");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        boolean alreadyFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
        if (alreadyFollowing) {
            return; // Idempotent
        }

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(targetUser)
                .build();
        followRepository.save(follow);

        // Notify the followed user
        notificationService.createNotification(targetUser, currentUser, NotificationType.FOLLOW, null);
    }

    @Transactional
    public void unfollowUser(UUID targetUserId, User currentUser) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        followRepository.findByFollowerAndFollowing(currentUser, targetUser)
                .ifPresent(followRepository::delete);
    }
}
