package com.empathai.user.controller;

import com.empathai.user.dto.auth.AuthResponse;
import com.empathai.user.dto.auth.LoginRequest;
import com.empathai.user.dto.auth.SetPasswordRequest;
import com.empathai.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Validates the token from the email link before showing the form
    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        Map<String, Object> result = authService.validateSetupToken(token);
        return ResponseEntity.ok(result);
    }

    // Student submits their new password
    @PostMapping("/set-password")
    public ResponseEntity<Map<String, String>> setPassword(@RequestBody SetPasswordRequest request) {
        authService.setPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password set successfully. You can now log in."));
    }
}