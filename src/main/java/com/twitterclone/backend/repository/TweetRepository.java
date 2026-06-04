package com.twitterclone.backend.repository;

import com.twitterclone.backend.dto.TweetResponse;
import com.twitterclone.backend.model.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {

    @Query("SELECT new com.twitterclone.backend.dto.TweetResponse(" +
           "t.id, t.content, t.author.id, t.author.username, t.author.avatarPlaceholder, t.createdAt, " +
           "(SELECT COUNT(l) FROM Like l WHERE l.tweet.id = t.id), " +
           "CASE WHEN (SELECT COUNT(l) FROM Like l WHERE l.tweet.id = t.id AND l.user.id = :currentUserId) > 0 THEN true ELSE false END, " +
           "t.replyCount, t.parentTweet.id) " +
           "FROM Tweet t WHERE t.parentTweet IS NULL AND t.author.id IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId) " +
           "ORDER BY t.createdAt DESC")
    Page<TweetResponse> findTimelineTweets(
            @Param("followerId") UUID followerId,
            @Param("currentUserId") UUID currentUserId,
            Pageable pageable
    );

    @Query("SELECT new com.twitterclone.backend.dto.TweetResponse(" +
           "t.id, t.content, t.author.id, t.author.username, t.author.avatarPlaceholder, t.createdAt, " +
           "(SELECT COUNT(l) FROM Like l WHERE l.tweet.id = t.id), " +
           "CASE WHEN (SELECT COUNT(l) FROM Like l WHERE l.tweet.id = t.id AND l.user.id = :currentUserId) > 0 THEN true ELSE false END, " +
           "t.replyCount, t.parentTweet.id) " +
           "FROM Tweet t WHERE t.id = :tweetId")
    Optional<TweetResponse> findTweetByIdAndCurrentUser(
            @Param("tweetId") UUID tweetId,
            @Param("currentUserId") UUID currentUserId
    );

    @Query("SELECT new com.twitterclone.backend.dto.TweetResponse(" +
           "t.id, t.content, t.author.id, t.author.username, t.author.avatarPlaceholder, t.createdAt, " +
           "(SELECT COUNT(l) FROM Like l WHERE l.tweet.id = t.id), " +
           "CASE WHEN (SELECT COUNT(l) FROM Like l WHERE l.tweet.id = t.id AND l.user.id = :currentUserId) > 0 THEN true ELSE false END, " +
           "t.replyCount, t.parentTweet.id) " +
           "FROM Tweet t WHERE t.parentTweet.id = :parentTweetId " +
           "ORDER BY t.createdAt ASC")
    Page<TweetResponse> findReplies(
            @Param("parentTweetId") UUID parentTweetId,
            @Param("currentUserId") UUID currentUserId,
            Pageable pageable
    );
}
