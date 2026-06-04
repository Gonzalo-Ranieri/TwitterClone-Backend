package com.twitterclone.backend.controller;

import com.twitterclone.backend.model.Role;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.FollowRepository;
import com.twitterclone.backend.repository.UserRepository;
import com.twitterclone.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User mainUser;
    private String mainUserToken;
    private User otherUser;

    @BeforeEach
    void setUp() {
        followRepository.deleteAll();
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
    void followUser_whenValid_shouldCreateFollow() throws Exception {
        assertFalse(followRepository.existsByFollowerAndFollowing(mainUser, otherUser));

        mockMvc.perform(post("/api/users/" + otherUser.getId() + "/follow")
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isOk());

        assertTrue(followRepository.existsByFollowerAndFollowing(mainUser, otherUser));
    }

    @Test
    void followUser_whenSelfFollow_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/" + mainUser.getId() + "/follow")
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void followUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(post("/api/users/" + nonExistentId + "/follow")
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void unfollowUser_whenFollowExists_shouldDeleteFollow() throws Exception {
        // Create follow relation first
        mockMvc.perform(post("/api/users/" + otherUser.getId() + "/follow")
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isOk());

        assertTrue(followRepository.existsByFollowerAndFollowing(mainUser, otherUser));

        // Unfollow
        mockMvc.perform(delete("/api/users/" + otherUser.getId() + "/follow")
                        .header("Authorization", "Bearer " + mainUserToken))
                .andExpect(status().isOk());

        assertFalse(followRepository.existsByFollowerAndFollowing(mainUser, otherUser));
    }
}
