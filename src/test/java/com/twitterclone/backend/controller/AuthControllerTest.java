package com.twitterclone.backend.controller;

import tools.jackson.databind.ObjectMapper;
import com.twitterclone.backend.dto.LoginRequest;
import com.twitterclone.backend.dto.RegisterRequest;
import com.twitterclone.backend.model.Role;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_whenValidRequest_shouldRegisterAndReturnToken() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .username("newuser")
                .password("password123")
                .bio("Hello world")
                .avatarPlaceholder("avatar.png")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.email", is("newuser@example.com")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void register_whenUsernameExists_shouldReturnBadRequest() throws Exception {
        User existingUser = User.builder()
                .email("existing@example.com")
                .username("existinguser")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .username("existinguser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El nombre de usuario ya está registrado")));
    }

    @Test
    void register_whenEmailExists_shouldReturnBadRequest() throws Exception {
        User existingUser = User.builder()
                .email("existing@example.com")
                .username("existinguser")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .username("newuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El email ya está registrado")));
    }

    @Test
    void register_whenFieldsInvalid_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .username("us") // too short
                .password("123") // too short
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_whenValidCredentials_shouldReturnToken() throws Exception {
        User user = User.builder()
                .email("loginuser@example.com")
                .username("loginuser")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(user);

        // Test login by username
        LoginRequest requestUsername = LoginRequest.builder()
                .usernameOrEmail("loginuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUsername)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("loginuser")));

        // Test login by email
        LoginRequest requestEmail = LoginRequest.builder()
                .usernameOrEmail("loginuser@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("loginuser")));
    }

    @Test
    void login_whenInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        User user = User.builder()
                .email("loginuser@example.com")
                .username("loginuser")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("loginuser")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales incorrectas"));
    }

    @Autowired
    private com.twitterclone.backend.security.JwtService jwtService;

    @Test
    void protectedRoute_withoutToken_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/secured/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedRoute_withValidToken_shouldPassFilterAndReturnNotFound() throws Exception {
        User user = User.builder()
                .email("testuser@example.com")
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/secured/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
