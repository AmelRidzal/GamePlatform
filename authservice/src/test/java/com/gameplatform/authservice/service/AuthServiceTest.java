package com.gameplatform.authservice.service;

import com.gameplatform.authservice.dto.AuthResponse;
import com.gameplatform.authservice.dto.LoginRequest;
import com.gameplatform.authservice.dto.RegisterRequest;
import com.gameplatform.authservice.entity.User;
import com.gameplatform.authservice.repository.UserRepository;
import com.gameplatform.authservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@test.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("existinguser");
        loginRequest.setPassword("password123");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@test.com");
        existingUser.setPassword("hashedPassword123");
        existingUser.setRole(User.Role.USER);
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("newuser", "USER")).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRole()).isEqualTo("USER");

        // verify password was hashed, never stored in plain text
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_shouldThrowWhenUsernameTaken() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Username already taken");

        // make sure we never tried to save if username is taken
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrowWhenEmailTaken() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsCorrect() {
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(true);
        when(jwtUtil.generateToken("existinguser", "USER")).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUsername()).isEqualTo("existinguser");
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void login_shouldThrowWhenPasswordIncorrect() {
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid password");
    }
}