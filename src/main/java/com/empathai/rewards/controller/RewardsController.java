package com.empathai.rewards.controller;

import com.empathai.rewards.dto.response.AchievementResponse;
import com.empathai.rewards.dto.response.BadgeResponse;
import com.empathai.rewards.service.RewardsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardsController {

    private final RewardsService rewardsService;

    // ══════════════════════════════════════════════════════════════════════
    // BADGES — Admin CRUD
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/badges")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        return ResponseEntity.ok(rewardsService.getAllBadges());
    }

    @PostMapping("/badges")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BadgeResponse> createBadge(
            @RequestParam("title")        String title,
            @RequestParam("triggerType")  String triggerType,
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
            @RequestParam("title")        String title,
            @RequestParam("triggerType")  String triggerType,
            @RequestParam("triggerTitle") String triggerTitle,
            @RequestParam(value = "triggerValue", required = false) String triggerValue,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(
                rewardsService.updateBadge(id, title, triggerType, triggerTitle, triggerValue, image));
    }

    @DeleteMapping("/badges/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long id) {
        rewardsService.deleteBadge(id);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // STUDENT BADGES — Read
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GET /api/rewards/students/{studentId}/badges
     *
     * Returns all badges the student has earned.
     * Each item includes: id, title, imageBase64, imageType, triggerType,
     * triggerTitle, triggerValue, earnedAt.
     *
     * Called by the frontend Dashboard BadgesModal via fetchStudentBadges(studentId).
     */
    @GetMapping("/students/{studentId}/badges")
    @PreAuthorize("hasAnyRole('STUDENT','SCHOOL_ADMIN','PSYCHOLOGIST','SUPER_ADMIN')")
    public ResponseEntity<List<BadgeResponse>> getStudentBadges(@PathVariable Long studentId) {
        return ResponseEntity.ok(rewardsService.getStudentBadges(studentId));
    }

    // ══════════════════════════════════════════════════════════════════════
    // ACHIEVEMENTS — Admin CRUD
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/achievements")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AchievementResponse>> getAllAchievements() {
        return ResponseEntity.ok(rewardsService.getAllAchievements());
    }

    @PostMapping("/achievements")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AchievementResponse> createAchievement(
            @RequestParam("title")       String title,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rewardsService.createAchievement(title, description, image));
    }

    @PutMapping("/achievements/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AchievementResponse> updateAchievement(
            @PathVariable Long id,
            @RequestParam("title")       String title,
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