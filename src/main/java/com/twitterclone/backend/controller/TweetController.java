package com.twitterclone.backend.controller;

import com.twitterclone.backend.dto.TweetRequest;
import com.twitterclone.backend.dto.TweetResponse;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.service.LikeService;
import com.twitterclone.backend.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TweetController {

    private final TweetService tweetService;
    private final LikeService likeService;

    @PostMapping("/tweets")
    public ResponseEntity<TweetResponse> createTweet(
            @Valid @RequestBody TweetRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        TweetResponse response = tweetService.createTweet(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> deleteTweet(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        tweetService.deleteTweet(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tweets/{id}/like")
    public ResponseEntity<Void> likeTweet(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        likeService.likeTweet(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tweets/{id}/like")
    public ResponseEntity<Void> unlikeTweet(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        likeService.unlikeTweet(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/timeline")
    public ResponseEntity<Page<TweetResponse>> getTimeline(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<TweetResponse> timeline = tweetService.getTimeline(currentUser, pageable);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/tweets/{id}")
    public ResponseEntity<TweetResponse> getTweet(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        TweetResponse tweet = tweetService.getTweetById(id, currentUser);
        return ResponseEntity.ok(tweet);
    }

    @GetMapping("/tweets/{id}/replies")
    public ResponseEntity<Page<TweetResponse>> getReplies(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<TweetResponse> replies = tweetService.getReplies(id, currentUser, pageable);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/users/{id}/tweets")
    public ResponseEntity<Page<TweetResponse>> getUserTweets(
            @PathVariable("id") UUID id,
            @RequestParam(value = "filter", defaultValue = "posts") String filter,
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<TweetResponse> userTweets = tweetService.getUserTweets(id, filter, currentUser, pageable);
        return ResponseEntity.ok(userTweets);
    }
}
