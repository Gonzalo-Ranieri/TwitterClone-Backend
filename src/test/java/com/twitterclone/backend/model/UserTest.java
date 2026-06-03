package com.twitterclone.backend.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .bio("My bio")
                .avatarPlaceholder("avatar.png")
                .roles(Set.of(Role.USER))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid user should not have violations");
    }

    @Test
    void whenEmailInvalid_thenViolationOccurs() {
        User user = User.builder()
                .email("invalid-email")
                .username("testuser")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Invalid email format should trigger violation");
        boolean hasEmailViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(hasEmailViolation);
    }

    @Test
    void whenUsernameTooShort_thenViolationOccurs() {
        User user = User.builder()
                .email("test@example.com")
                .username("us")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Short username should trigger violation");
        boolean hasUsernameViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        assertTrue(hasUsernameViolation);
    }

    @Test
    void whenPasswordTooShort_thenViolationOccurs() {
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .password("12345")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Short password should trigger violation");
        boolean hasPasswordViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(hasPasswordViolation);
    }

    @Test
    void testUserDetailsMethods() {
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .roles(Set.of(Role.USER, Role.ADMIN))
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        assertEquals("password123", user.getPassword());
        assertEquals("testuser", user.getUsername());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }
}
