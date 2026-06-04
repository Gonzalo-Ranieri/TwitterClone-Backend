package com.twitterclone.backend.controller;

import com.twitterclone.backend.dto.AuthResponse;
import com.twitterclone.backend.dto.LoginRequest;
import com.twitterclone.backend.dto.RegisterRequest;
import com.twitterclone.backend.model.Role;
import com.twitterclone.backend.model.User;
import com.twitterclone.backend.repository.UserRepository;
import com.twitterclone.backend.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya está registrado");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }

        String avatar = request.getAvatarPlaceholder() != null ? request.getAvatarPlaceholder().trim() : null;
        if (avatar != null && avatar.isEmpty()) {
            avatar = null;
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .bio(request.getBio())
                .avatarPlaceholder(avatar)
                .roles(Set.of(Role.USER))
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userRepository.findByUsername(request.getUsernameOrEmail())
                    .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()
                    )
            );

            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build());
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
    }
}
