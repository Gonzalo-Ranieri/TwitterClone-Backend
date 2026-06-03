package com.twitterclone.backend.security;

import com.twitterclone.backend.model.Role;
import com.twitterclone.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        userDetails = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .roles(Set.of(Role.USER))
                .build();
    }

    @Test
    void whenGenerateToken_thenTokenIsCreated() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void whenExtractUsername_thenCorrectUsernameIsReturned() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void whenTokenValid_thenIsValidTokenReturnsTrue() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void whenUsernameDiffers_thenIsValidTokenReturnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = User.builder()
                .username("otheruser")
                .build();
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }
}
