package com.twitterclone.backend.service;

import com.twitterclone.backend.model.Like;
import com.twitterclone.backend.model.NotificationType;
import com.twitterclone.backend.model.Tweet;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.LikeRepository;
import com.twitterclone.backend.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final TweetRepository tweetRepository;
    private final NotificationService notificationService;

    @Transactional
    public void likeTweet(UUID tweetId, User currentUser) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet no encontrado"));

        boolean alreadyLiked = likeRepository.existsByUserAndTweet(currentUser, tweet);
        if (alreadyLiked) {
            return; // Idempotent
        }

        Like like = Like.builder()
                .user(currentUser)
                .tweet(tweet)
                .build();
        likeRepository.save(like);

        // Notify the author of the tweet
        notificationService.createNotification(tweet.getAuthor(), currentUser, NotificationType.LIKE, tweet.getId());
    }

    @Transactional
    public void unlikeTweet(UUID tweetId, User currentUser) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet no encontrado"));

        likeRepository.findByUserAndTweet(currentUser, tweet)
                .ifPresent(likeRepository::delete);
    }
}
