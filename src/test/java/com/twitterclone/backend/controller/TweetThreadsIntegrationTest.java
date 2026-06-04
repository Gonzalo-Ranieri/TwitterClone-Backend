package com.twitterclone.backend.controller;

import tools.jackson.databind.ObjectMapper;
import com.twitterclone.backend.dto.TweetRequest;
import com.twitterclone.backend.model.Role;
import com.twitterclone.backend.model.Tweet;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.LikeRepository;
import com.twitterclone.backend.repository.TweetRepository;
import com.twitterclone.backend.repository.UserRepository;
import com.twitterclone.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TweetThreadsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User mainUser;
    private String mainUserToken;
    private User otherUser;

    @BeforeEach
    void setUp() {
        likeRepository.deleteAll();
        tweetRepository.deleteAll();
        userRepository.deleteAll();

        mainUser = User.builder()
                .email("main@example.com")
                .username("mainuser")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(mainUser);
        mainUserToken = jwtService.generateToken(mainUser);

        otherUser = User.builder()
                .email("other@example.com")
                .username("otheruser")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(otherUser);
    }

    @Test
    void testReplyThreadFlow() throws Exception {
        // 1. Create a parent tweet
        Tweet parent = Tweet.builder()
                .content("Parent tweet content")
                .author(mainUser)
                .build();
        parent = tweetRepository.save(parent);

        // 2. Create a reply using POST /api/tweets
        TweetRequest replyRequest = TweetRequest.builder()
                .content("This is a reply to the parent tweet")
                .parentTweetId(parent.getId())
                .build();

        mockMvc.perform(post("/api/tweets")
                        .header("Authorization", "Bearer " + mainUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.content", is("This is a reply to the parent tweet")))
                .andExpect(jsonPath("$.parentTweetId", is(parent.getId().toString())))
                .andExpect(jsonPath("$.replyCount", is(0)));

        // 3. Verify parent's replyCount has incremented
        mockMvc.perform(get("/api/tweets/" + parent.getId())
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(parent.getId().toString())))
                .andExpect(jsonPath("$.replyCount", is(1)));

        // 4. Retrieve replies using GET /api/tweets/{id}/replies
        mockMvc.perform(get("/api/tweets/" + parent.getId() + "/replies")
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].content", is("This is a reply to the parent tweet")))
                .andExpect(jsonPath("$.content[0].parentTweetId", is(parent.getId().toString())));

        // 5. Delete reply and verify parent's replyCount decrements
        Tweet replyTweet = tweetRepository.findAll().stream()
                .filter(t -> t.getParentTweet() != null)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/tweets/" + replyTweet.getId())
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isNoContent());

        // Verify parent count decremented to 0
        mockMvc.perform(get("/api/tweets/" + parent.getId())
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replyCount", is(0)));
    }
}
