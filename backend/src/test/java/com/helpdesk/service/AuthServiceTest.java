package com.helpdesk.service;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.RegisterRequest;
import com.helpdesk.exception.ConflictException;
import com.helpdesk.exception.UnauthorizedException;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.JwtService;
import com.helpdesk.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerAlwaysCreatesCustomer() {
        when(userRepository.existsByEmail("lydia@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount user = invocation.getArgument(0);
            user.setId(4L);
            user.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return user;
        });
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("token");
        when(jwtService.expiresAt(any(Instant.class))).thenReturn(Instant.parse("2026-01-01T08:00:00Z"));

        AuthResponse response = authService.register(new RegisterRequest(
                "  Lydia@Example.com ",
                "Password1",
                " Lydia ",
                "Njeri"
        ));

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertEquals("lydia@example.com", captor.getValue().getEmail());
        assertEquals(Role.CUSTOMER, captor.getValue().getRole());
        assertEquals("Lydia", captor.getValue().getFirstName());
        assertEquals(Role.CUSTOMER, response.user().role());
        assertEquals("token", response.token());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("lydia@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(new RegisterRequest(
                "lydia@example.com",
                "Password1",
                "Lydia",
                "Njeri"
        )));
    }

    @Test
    void loginRejectsBadPassword() {
        UserAccount user = new UserAccount();
        user.setEmail("lydia@example.com");
        user.setPasswordHash("hashed");
        user.setEnabled(true);
        user.setRole(Role.CUSTOMER);
        when(userRepository.findByEmail("lydia@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(
                new com.helpdesk.dto.LoginRequest("lydia@example.com", "wrong")));
    }
}
