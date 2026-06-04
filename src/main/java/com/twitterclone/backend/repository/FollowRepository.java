package com.twitterclone.backend.repository;

import com.twitterclone.backend.model.Follow;
import com.twitterclone.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerAndFollowing(User follower, User following);
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    long countByFollowerId(UUID followerId);
    long countByFollowingId(UUID followingId);
}
