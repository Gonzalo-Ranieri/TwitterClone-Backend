package com.twitterclone.backend.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TweetTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        User author = User.builder()
                .email("author@example.com")
                .username("author")
                .password("password123")
                .build();

        Tweet tweet = Tweet.builder()
                .content("Este es un tweet válido de prueba.")
                .author(author)
                .build();

        Set<ConstraintViolation<Tweet>> violations = validator.validate(tweet);
        assertTrue(violations.isEmpty(), "Valid tweet should not have violations");
    }

    @Test
    void whenContentIsBlank_thenViolationOccurs() {
        User author = User.builder()
                .email("author@example.com")
                .username("author")
                .password("password123")
                .build();

        Tweet tweet = Tweet.builder()
                .content("   ")
                .author(author)
                .build();

        Set<ConstraintViolation<Tweet>> violations = validator.validate(tweet);
        assertFalse(violations.isEmpty(), "Blank content should trigger violation");
        boolean hasContentViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("content"));
        assertTrue(hasContentViolation);
    }

    @Test
    void whenContentExceedsMaxLength_thenViolationOccurs() {
        User author = User.builder()
                .email("author@example.com")
                .username("author")
                .password("password123")
                .build();

        // 281 characters
        String longContent = "a".repeat(281);

        Tweet tweet = Tweet.builder()
                .content(longContent)
                .author(author)
                .build();

        Set<ConstraintViolation<Tweet>> violations = validator.validate(tweet);
        assertFalse(violations.isEmpty(), "Content exceeding 280 chars should trigger violation");
        boolean hasContentViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("content"));
        assertTrue(hasContentViolation);
    }
}
