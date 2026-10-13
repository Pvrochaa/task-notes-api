package com.cristian.tasknotesapi.service;

import com.cristian.tasknotesapi.domain.user.Role;
import com.cristian.tasknotesapi.domain.user.User;
import com.cristian.tasknotesapi.repository.UserRepository;
import com.cristian.tasknotesapi.security.JwtService;
import com.cristian.tasknotesapi.web.dto.AuthResponse;
import com.cristian.tasknotesapi.web.dto.LoginRequest;
import com.cristian.tasknotesapi.web.dto.RegisterRequest;
import com.cristian.tasknotesapi.web.exception.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UsernameAlreadyExistsException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return AuthResponse.bearer(jwtService.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("User vanished after authentication"));

        return AuthResponse.bearer(jwtService.generateToken(user));
    }
}
