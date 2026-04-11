package com.empathai.rewards.service;

import com.empathai.rewards.dto.response.AchievementResponse;
import com.empathai.rewards.dto.response.BadgeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RewardsService {

    // ── Badges ────────────────────────────────────────────────────────────
    List<BadgeResponse> getAllBadges();
    BadgeResponse createBadge(String title, String triggerType, String triggerTitle, MultipartFile image);
    BadgeResponse updateBadge(Long id, String title, String triggerType, String triggerTitle, MultipartFile image);
    void deleteBadge(Long id);

    // ── Student Badges ────────────────────────────────────────────────────
    List<BadgeResponse> getStudentBadges(Long studentId);

    // ── Badge Award Triggers ──────────────────────────────────────────────
    // Called by AuthService after each login to check & award login-milestone badges.
    void checkAndAwardLoginBadges(Long studentId, int totalLogins);

    // Called by InterventionController after each session to check & award
    // intervention-milestone badges.
    void checkAndAwardInterventionBadges(Long studentId, int totalInterventions);

    // ── Achievements ──────────────────────────────────────────────────────
    List<AchievementResponse> getAllAchievements();
    AchievementResponse createAchievement(String title, String description, MultipartFile image);
    AchievementResponse updateAchievement(Long id, String title, String description, MultipartFile image);
    void deleteAchievement(Long id);
}