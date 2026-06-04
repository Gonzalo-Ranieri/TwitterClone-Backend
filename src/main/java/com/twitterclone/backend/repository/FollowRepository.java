package com.twitterclone.backend.repository;

import com.twitterclone.backend.model.Follow;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.dto.FollowUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerAndFollowing(User follower, User following);
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    long countByFollowerId(UUID followerId);
    long countByFollowingId(UUID followingId);

    @Query("SELECT new com.twitterclone.backend.dto.FollowUserResponse(" +
           "f.follower.id, f.follower.username, f.follower.bio, f.follower.avatarPlaceholder, " +
           "CASE WHEN (SELECT COUNT(cf) FROM Follow cf WHERE cf.follower.id = :currentUserId AND cf.following.id = f.follower.id) > 0 THEN true ELSE false END) " +
           "FROM Follow f WHERE f.following.id = :userId")
    Page<FollowUserResponse> findFollowers(
            @Param("userId") UUID userId,
            @Param("currentUserId") UUID currentUserId,
            Pageable pageable
    );

    @Query("SELECT new com.twitterclone.backend.dto.FollowUserResponse(" +
           "f.following.id, f.following.username, f.following.bio, f.following.avatarPlaceholder, " +
           "CASE WHEN (SELECT COUNT(cf) FROM Follow cf WHERE cf.follower.id = :currentUserId AND cf.following.id = f.following.id) > 0 THEN true ELSE false END) " +
           "FROM Follow f WHERE f.follower.id = :userId")
    Page<FollowUserResponse> findFollowing(
            @Param("userId") UUID userId,
            @Param("currentUserId") UUID currentUserId,
            Pageable pageable
    );
}
