package com.twitterclone.backend.repository;

import com.twitterclone.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
