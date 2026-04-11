package com.empathai.user.service;

import com.empathai.rewards.service.RewardsService;
import com.empathai.user.dto.auth.AuthResponse;
import com.empathai.user.dto.auth.LoginRequest;
import com.empathai.user.entity.Student;
import com.empathai.user.entity.User;
import com.empathai.user.exception.EmpathaiException;
import com.empathai.user.repository.StudentRepository;
import com.empathai.user.repository.UserRepository;
import com.empathai.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository        userRepository;
    private final StudentRepository     studentRepository;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService           userService;
    private final RewardsService        rewardsService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Support login by email OR username
        String loginId = request.getEmail();

        User userLookup = userRepository.findByEmail(loginId)
                .or(() -> userRepository.findByUsername(loginId))
                .orElseThrow(() -> new EmpathaiException("Invalid credentials", "AUTH_FAILURE"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLookup.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new EmpathaiException("Invalid credentials", "AUTH_FAILURE");
        }

        // ── Increment loginCount and auto-award badges for students ───────
        if (userLookup instanceof Student) {
            studentRepository.incrementLoginCount(userLookup.getId());

            // Re-fetch to read the freshly incremented count
            Student refreshed = studentRepository.findById(userLookup.getId())
                    .orElse((Student) userLookup);
            int newCount = refreshed.getLoginCount() != null ? refreshed.getLoginCount() : 1;

            rewardsService.checkAndAwardLoginBadges(userLookup.getId(), newCount);
            log.info("Student {} logged in — loginCount now {}", userLookup.getId(), newCount);
        }

        String jwtToken = jwtService.generateToken(userLookup);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(userService.mapToFullResponse(userLookup))
                .build();
    }
}