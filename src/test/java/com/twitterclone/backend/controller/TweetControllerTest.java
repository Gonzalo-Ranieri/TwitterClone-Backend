package com.twitterclone.backend.controller;

import tools.jackson.databind.ObjectMapper;
import com.twitterclone.backend.dto.TweetRequest;
import com.twitterclone.backend.model.Like;
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
class TweetControllerTest {

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
    private String otherUserToken;

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
        otherUserToken = jwtService.generateToken(otherUser);
    }

    @Test
    void createTweet_whenValidRequest_shouldCreateAndReturnTweet() throws Exception {
        TweetRequest request = TweetRequest.builder()
                .content("Hola Twitter Clone!")
                .build();

        mockMvc.perform(post("/api/tweets")
                        .header("Authorization", "Bearer " + mainUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.content", is("Hola Twitter Clone!")))
                .andExpect(jsonPath("$.authorUsername", is("mainuser")))
                .andExpect(jsonPath("$.likeCount", is(0)))
                .andExpect(jsonPath("$.liked", is(false)));
    }

    @Test
    void createTweet_whenNoToken_shouldReturnForbidden() throws Exception {
        TweetRequest request = TweetRequest.builder()
                .content("Sin token")
                .build();

        mockMvc.perform(post("/api/tweets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTweet_whenContentBlank_shouldReturnBadRequest() throws Exception {
        TweetRequest request = TweetRequest.builder()
                .content("   ")
                .build();

        mockMvc.perform(post("/api/tweets")
                        .header("Authorization", "Bearer " + mainUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTweet_whenAuthor_shouldDeleteSuccessfully() throws Exception {
        Tweet tweet = Tweet.builder()
                .content("Mi tweet")
                .author(mainUser)
                .build();
        tweet = tweetRepository.save(tweet);

        mockMvc.perform(delete("/api/tweets/" + tweet.getId())
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isNoContent());

        assertFalse(tweetRepository.existsById(tweet.getId()));
    }

    @Test
    void deleteTweet_whenNotAuthor_shouldReturnForbidden() throws Exception {
        Tweet tweet = Tweet.builder()
                .content("Tweet de main")
                .author(mainUser)
                .build();
        tweet = tweetRepository.save(tweet);

        mockMvc.perform(delete("/api/tweets/" + tweet.getId())
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());

        assertTrue(tweetRepository.existsById(tweet.getId()));
    }

    @Test
    void likeAndUnlikeTweet_shouldUpdateLikesCount() throws Exception {
        Tweet tweet = Tweet.builder()
                .content("Tweet para likes")
                .author(otherUser)
                .build();
        tweet = tweetRepository.save(tweet);

        // Initially no likes
        assertTrue(likeRepository.findAll().isEmpty());

        // Like the tweet
        mockMvc.perform(post("/api/tweets/" + tweet.getId() + "/like")
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isOk());

        assertEquals(1, likeRepository.findAll().size());
        assertTrue(likeRepository.existsByUserAndTweet(mainUser, tweet));

        // Unlike the tweet
        mockMvc.perform(delete("/api/tweets/" + tweet.getId() + "/like")
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isOk());

        assertTrue(likeRepository.findAll().isEmpty());
    }

    @Test
    void getUserTweets_withFilters_shouldReturnCorrectTweets() throws Exception {
        // 1. Original tweet by mainUser
        Tweet originalTweet = Tweet.builder()
                .content("Original Tweet")
                .author(mainUser)
                .build();
        originalTweet = tweetRepository.save(originalTweet);

        // 2. Reply tweet by mainUser
        Tweet replyTweet = Tweet.builder()
                .content("Reply Tweet")
                .author(mainUser)
                .parentTweet(originalTweet)
                .build();
        replyTweet = tweetRepository.save(replyTweet);

        // 3. Other tweet liked by mainUser
        Tweet otherTweet = Tweet.builder()
                .content("Other User Tweet")
                .author(otherUser)
                .build();
        otherTweet = tweetRepository.save(otherTweet);

        Like like = Like.builder()
                .user(mainUser)
                .tweet(otherTweet)
                .build();
        likeRepository.save(like);

        // Query default / posts filter -> should return only original tweet
        mockMvc.perform(get("/api/users/" + mainUser.getId() + "/tweets")
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].content", is("Original Tweet")));

        // Query replies filter -> should return only reply tweet
        mockMvc.perform(get("/api/users/" + mainUser.getId() + "/tweets")
                        .header("Authorization", "Bearer " + mainUserToken)
                        .param("filter", "replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].content", is("Reply Tweet")));

        // Query likes filter -> should return only liked tweet
        mockMvc.perform(get("/api/users/" + mainUser.getId() + "/tweets")
                        .header("Authorization", "Bearer " + mainUserToken)
                        .param("filter", "likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].content", is("Other User Tweet")));

        // Query all filter -> should return both posts and replies (2 tweets)
        mockMvc.perform(get("/api/users/" + mainUser.getId() + "/tweets")
                        .header("Authorization", "Bearer " + mainUserToken)
                        .param("filter", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }
}
