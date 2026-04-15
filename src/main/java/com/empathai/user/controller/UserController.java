package com.empathai.user.controller;

import com.empathai.user.dto.user.*;
import com.empathai.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth.getName();

        return ResponseEntity.ok(
                userService.getAllUsers().stream()
                        .filter(u -> principal.equals(u.getEmail()) || principal.equals(u.getUsername()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("User not found"))
        );
    }

    @GetMapping("/students")
    public ResponseEntity<Page<StudentSummaryResponse>> getStudents(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(userService.getStudentPage(school, search, page, size));
    }

    @GetMapping("/school-admins")
    public ResponseEntity<Page<SchoolAdminResponse>> getSchoolAdmins(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(userService.getSchoolAdminPage(search, page, size));
    }

    @GetMapping("/psychologists")
    public ResponseEntity<Page<PsychologistResponse>> getPsychologists(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(userService.getPsychologistPage(search, page, size));
    }

    @GetMapping("/content-admins")
    public ResponseEntity<Page<ContentAdminResponse>> getContentAdmins(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(userService.getContentAdminPage(search, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/time-spent")
    public ResponseEntity<Void> updateTimeSpent(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long seconds = body.get("seconds");
        if (seconds != null && seconds > 0) {
            userService.incrementTimeSpent(id, seconds);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/users/{id}/intervention-complete
     * Increments student's interventionSessionCount by 1.
     * Called when student completes a wellness activity (meditation timer).
     */
    @PostMapping("/{id}/intervention-complete")
    public ResponseEntity<Map<String, Object>> completeIntervention(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String activityType = body.getOrDefault("activityType", "unknown");
        int newCount = userService.incrementInterventionAndAwardBadges(id, activityType);
        return ResponseEntity.ok(Map.of(
                "interventionSessionCount", newCount,
                "activityType", activityType
        ));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id) {
        String newPassword = generateTempPassword();
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(Map.of("newPassword", newPassword));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}