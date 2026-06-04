package com.twitterclone.backend.repository;

import com.twitterclone.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.twitterclone.backend.dto.FollowUserResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.id <> :userId AND u.id NOT IN " +
           "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId)")
    List<User> findFollowSuggestions(@Param("userId") UUID userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT new com.twitterclone.backend.dto.FollowUserResponse(" +
           "u.id, u.username, u.bio, u.avatarPlaceholder, " +
           "CASE WHEN (SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :currentUserId AND f.following.id = u.id) > 0 THEN true ELSE false END) " +
           "FROM User u " +
           "WHERE u.id <> :currentUserId AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<FollowUserResponse> searchUsers(
            @Param("query") String query,
            @Param("currentUserId") UUID currentUserId
    );
}
