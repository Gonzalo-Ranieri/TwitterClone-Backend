package com.twitterclone.backend.repository;

import com.twitterclone.backend.model.Like;
import com.twitterclone.backend.model.Tweet;
import com.twitterclone.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    boolean existsByUserAndTweet(User user, Tweet tweet);
    long countByTweet(Tweet tweet);
    Optional<Like> findByUserAndTweet(User user, Tweet tweet);
}
