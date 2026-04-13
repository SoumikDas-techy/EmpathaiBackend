package com.empathai.rewards.controller;

import com.empathai.rewards.dto.response.AchievementResponse;
import com.empathai.rewards.dto.response.BadgeResponse;
import com.empathai.rewards.service.RewardsService;
import com.empathai.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardsController {

    private static final Logger logger = LoggerFactory.getLogger(RewardsController.class);
    private final RewardsService rewardsService;

    // ══════════════════════════════════════════════════════════════════════
    // BADGES (ADMIN)
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/badges")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        return ResponseEntity.ok(rewardsService.getAllBadges());
    }

    @PostMapping("/badges")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BadgeResponse> createBadge(
            @RequestParam("title") String title,
            @RequestParam("triggerType") String triggerType,
            @RequestParam("triggerTitle") String triggerTitle,
            @RequestParam(value = "triggerValue", required = false) String triggerValue,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rewardsService.createBadge(title, triggerType, triggerTitle, triggerValue, image));
    }

    @PutMapping("/badges/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BadgeResponse> updateBadge(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("triggerType") String triggerType,
            @RequestParam("triggerTitle") String triggerTitle,
            @RequestParam(value = "triggerValue", required = false) String triggerValue,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(rewardsService.updateBadge(id, title, triggerType, triggerTitle, triggerValue, image));
    }

    @DeleteMapping("/badges/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long id) {
        rewardsService.deleteBadge(id);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // STUDENT BADGES
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Student fetches their OWN badges — ID comes from JWT, never from URL.
     */
    @GetMapping("/students/me/badges")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<BadgeResponse>> getMyBadges() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            List<BadgeResponse> badges = rewardsService.getStudentBadges(user.getId());
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            logger.error("Error fetching badges for student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Admin / staff fetch any student's badges by ID.
     */
    @GetMapping("/students/{studentId}/badges")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN', 'PSYCHOLOGIST')")
    public ResponseEntity<List<BadgeResponse>> getStudentBadges(@PathVariable Long studentId) {
        return ResponseEntity.ok(rewardsService.getStudentBadges(studentId));
    }

    // ══════════════════════════════════════════════════════════════════════
    // ACHIEVEMENTS (ADMIN)
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/achievements")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AchievementResponse>> getAllAchievements() {
        return ResponseEntity.ok(rewardsService.getAllAchievements());
    }

    @PostMapping("/achievements")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AchievementResponse> createAchievement(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rewardsService.createAchievement(title, description, image));
    }

    @PutMapping("/achievements/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AchievementResponse> updateAchievement(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(rewardsService.updateAchievement(id, title, description, image));
    }

    @DeleteMapping("/achievements/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAchievement(@PathVariable Long id) {
        rewardsService.deleteAchievement(id);
        return ResponseEntity.noContent().build();
    }
}