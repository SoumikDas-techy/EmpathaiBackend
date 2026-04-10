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

    /**
     * GET /api/users/students
     * Returns Page<StudentSummaryResponse>:
     *   id, name, email, username, active, school, className, rollNo
     * Previously returned full UserResponse including audit fields + all student fields.
     */
    @GetMapping("/students")
    public ResponseEntity<Page<StudentSummaryResponse>> getStudents(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(userService.getStudentPage(school, search, page, size));
    }

    /**
     * GET /api/users/school-admins
     * Returns Page<SchoolAdminResponse>:
     *   id, name, email, username, active, schoolId, school
     * Previously returned full UserResponse including audit + student-specific fields.
     */
    @GetMapping("/school-admins")
    public ResponseEntity<Page<SchoolAdminResponse>> getSchoolAdmins(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(userService.getSchoolAdminPage(search, page, size));
    }

    /**
     * GET /api/users/psychologists
     * Returns Page<PsychologistResponse>:
     *   id, name, email, username, phoneNumber, active
     * Previously returned full UserResponse with null student/school fields.
     */
    @GetMapping("/psychologists")
    public ResponseEntity<Page<PsychologistResponse>> getPsychologists(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(userService.getPsychologistPage(search, page, size));
    }

    /**
     * GET /api/users/content-admins
     * Returns Page<ContentAdminResponse>:
     *   id, name, email, username, phoneNumber, active
     * Previously returned full UserResponse with null student/school fields.
     */
    @GetMapping("/content-admins")
    public ResponseEntity<Page<ContentAdminResponse>> getContentAdmins(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(userService.getContentAdminPage(search, page, size));
    }

    /**
     * GET /api/users/{id}
     * Full user detail — used by edit screens.
     * Returns UserResponse (no audit fields — removed from that DTO).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
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
