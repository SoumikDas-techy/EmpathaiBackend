package com.empathai.user.service;

import com.empathai.user.dto.auth.AuthResponse;
import com.empathai.user.dto.auth.LoginRequest;
import com.empathai.user.entity.User;
import com.empathai.user.exception.EmpathaiException;
import com.empathai.user.repository.UserRepository;
import com.empathai.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthResponse login(LoginRequest request) {
        // Support login by email OR username
        String loginId = request.getEmail();

        // Look up user by email first, then try username
        User userLookup = userRepository.findByEmail(loginId)
                .or(() -> userRepository.findByUsername(loginId))
                .orElseThrow(() -> new EmpathaiException("Invalid credentials", "AUTH_FAILURE"));

        // Spring Security authenticates using the stored email
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLookup.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new EmpathaiException("Invalid credentials", "AUTH_FAILURE");
        }

        String jwtToken = jwtService.generateToken(userLookup);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(userService.mapToFullResponse(userLookup))
                .build();
    }
}
