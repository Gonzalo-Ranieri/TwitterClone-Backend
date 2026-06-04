package com.twitterclone.backend.service;

import com.twitterclone.backend.dto.TweetRequest;
import com.twitterclone.backend.dto.TweetResponse;
import com.twitterclone.backend.model.Tweet;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetRepository tweetRepository;

    @Transactional
    public TweetResponse createTweet(TweetRequest request, User currentUser) {
        Tweet parentTweet = null;
        if (request.getParentTweetId() != null) {
            parentTweet = tweetRepository.findById(request.getParentTweetId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet padre no encontrado"));
            parentTweet.setReplyCount(parentTweet.getReplyCount() + 1);
        }

        Tweet tweet = Tweet.builder()
                .content(request.getContent())
                .author(currentUser)
                .parentTweet(parentTweet)
                .build();
        Tweet savedTweet = tweetRepository.save(tweet);
        return TweetResponse.builder()
                .id(savedTweet.getId())
                .content(savedTweet.getContent())
                .authorId(currentUser.getId())
                .authorUsername(currentUser.getUsername())
                .authorAvatarPlaceholder(currentUser.getAvatarPlaceholder())
                .createdAt(savedTweet.getCreatedAt())
                .likeCount(0)
                .liked(false)
                .replyCount(0)
                .parentTweetId(parentTweet != null ? parentTweet.getId() : null)
                .build();
    }

    @Transactional
    public void deleteTweet(UUID tweetId, User currentUser) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet no encontrado"));

        if (!tweet.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este tweet");
        }

        if (tweet.getParentTweet() != null) {
            Tweet parent = tweet.getParentTweet();
            parent.setReplyCount(Math.max(0, parent.getReplyCount() - 1));
        }

        tweetRepository.delete(tweet);
    }

    @Transactional(readOnly = true)
    public TweetResponse getTweetById(UUID tweetId, User currentUser) {
        return tweetRepository.findTweetByIdAndCurrentUser(tweetId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet no encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<TweetResponse> getReplies(UUID parentTweetId, User currentUser, Pageable pageable) {
        if (!tweetRepository.existsById(parentTweetId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet no encontrado");
        }
        return tweetRepository.findReplies(parentTweetId, currentUser.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<TweetResponse> getTimeline(User currentUser, Pageable pageable) {
        return tweetRepository.findTimelineTweets(currentUser.getId(), currentUser.getId(), pageable);
    }
}
