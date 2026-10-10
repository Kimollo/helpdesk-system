package com.helpdesk.service;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.LoginRequest;
import com.helpdesk.dto.RegisterRequest;
import com.helpdesk.dto.UserResponse;
import com.helpdesk.exception.ConflictException;
import com.helpdesk.exception.UnauthorizedException;
import com.helpdesk.mapper.ApiMapper;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.JwtService;
import com.helpdesk.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        return issueToken(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(UserPrincipal actor) {
        UserAccount user = userRepository.findById(actor.getId())
                .orElseThrow(() -> new UnauthorizedException("Authentication is required"));
        return ApiMapper.toUser(user);
    }

    private AuthResponse issueToken(UserAccount user) {
        UserPrincipal principal = UserPrincipal.from(user);
        Instant now = Instant.now();
        return new AuthResponse(
                jwtService.generateToken(principal),
                "Bearer",
                jwtService.expiresAt(now),
                ApiMapper.toUser(user)
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
