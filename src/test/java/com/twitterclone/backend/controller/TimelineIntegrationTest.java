package com.twitterclone.backend.controller;

import com.twitterclone.backend.model.Follow;
import com.twitterclone.backend.model.Role;
import com.twitterclone.backend.model.Tweet;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.FollowRepository;
import com.twitterclone.backend.repository.TweetRepository;
import com.twitterclone.backend.repository.UserRepository;
import com.twitterclone.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TimelineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User currentUser;
    private String token;
    private User followedUser1;
    private User followedUser2;
    private User nonFollowedUser;

    @BeforeEach
    void setUp() throws Exception {
        followRepository.deleteAll();
        tweetRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create users
        currentUser = User.builder()
                .email("current@example.com")
                .username("current")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(currentUser);
        token = jwtService.generateToken(currentUser);

        followedUser1 = User.builder()
                .email("followed1@example.com")
                .username("followed1")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(followedUser1);

        followedUser2 = User.builder()
                .email("followed2@example.com")
                .username("followed2")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(followedUser2);

        nonFollowedUser = User.builder()
                .email("nonfollowed@example.com")
                .username("nonfollowed")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(nonFollowedUser);

        // 2. Set up follows (currentUser follows followedUser1 and followedUser2)
        followRepository.save(Follow.builder().follower(currentUser).following(followedUser1).build());
        followRepository.save(Follow.builder().follower(currentUser).following(followedUser2).build());

        // 3. Post tweets with artificial delays/timestamps to control order
        Tweet tweet1 = Tweet.builder().content("Tweet from followed 1").author(followedUser1).build();
        tweetRepository.save(tweet1);
        // Sleep 10ms to guarantee distinct createdAt timestamps
        Thread.sleep(10);

        Tweet tweet2 = Tweet.builder().content("Tweet from followed 2").author(followedUser2).build();
        tweetRepository.save(tweet2);
        Thread.sleep(10);

        Tweet tweet3 = Tweet.builder().content("Another tweet from followed 1").author(followedUser1).build();
        tweetRepository.save(tweet3);
        Thread.sleep(10);

        // Tweet from non-followed user (should NOT appear on timeline)
        Tweet tweet4 = Tweet.builder().content("Tweet from non-followed").author(nonFollowedUser).build();
        tweetRepository.save(tweet4);
    }

    @Test
    void getTimeline_shouldReturnTweetsOnlyFromFollowedUsersInDescOrder() throws Exception {
        // We expect:
        // 1. "Another tweet from followed 1" (tweet3) - newest
        // 2. "Tweet from followed 2" (tweet2)
        // 3. "Tweet from followed 1" (tweet1) - oldest
        // Total of 3 tweets on timeline (excluding tweet4)

        mockMvc.perform(get("/api/timeline")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].content", is("Another tweet from followed 1")))
                .andExpect(jsonPath("$.content[0].authorUsername", is("followed1")))
                .andExpect(jsonPath("$.content[1].content", is("Tweet from followed 2")))
                .andExpect(jsonPath("$.content[1].authorUsername", is("followed2")))
                .andExpect(jsonPath("$.content[2].content", is("Tweet from followed 1")))
                .andExpect(jsonPath("$.content[2].authorUsername", is("followed1")))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void getTimeline_withPagination_shouldReturnPagedResults() throws Exception {
        // Page 0, size 2: should return the 2 newest tweets (tweet3, tweet2)
        mockMvc.perform(get("/api/timeline")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].content", is("Another tweet from followed 1")))
                .andExpect(jsonPath("$.content[1].content", is("Tweet from followed 2")))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.number", is(0)));

        // Page 1, size 2: should return the oldest tweet (tweet1)
        mockMvc.perform(get("/api/timeline")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].content", is("Tweet from followed 1")))
                .andExpect(jsonPath("$.number", is(1)));
    }
}
