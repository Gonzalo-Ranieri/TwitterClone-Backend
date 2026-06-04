package com.twitterclone.backend.controller;

import com.twitterclone.backend.model.Role;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.UserRepository;
import com.twitterclone.backend.security.JwtService;
import com.twitterclone.backend.service.SseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SseService sseService;

    private User testUser;
    private String token;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .email("notif@example.com")
                .username("notifuser")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(testUser);
        token = jwtService.generateToken(testUser);
    }

    @Test
    void streamNotifications_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/notifications/stream"))
                .andExpect(status().isForbidden());
    }

    @Test
    void streamNotifications_whenAuthenticated_shouldReturnSseEmitter() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/notifications/stream")
                        .header("Authorization", "Bearer " + token))
                .andExpect(request().asyncStarted())
                .andReturn();

        SseEmitter emitter = sseService.getEmitter(testUser.getId());
        assertNotNull(emitter);
        emitter.complete();
    }
}
