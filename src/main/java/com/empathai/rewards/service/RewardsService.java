package com.empathai.rewards.service;

import com.empathai.rewards.dto.response.AchievementResponse;
import com.empathai.rewards.dto.response.BadgeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RewardsService {

    // ── Badges (admin CRUD) ───────────────────────────────────────────────
    List<BadgeResponse> getAllBadges();
    BadgeResponse createBadge(String title, String triggerType, String triggerTitle,
                              String triggerValue, MultipartFile image);
    BadgeResponse updateBadge(Long id, String title, String triggerType, String triggerTitle,
                              String triggerValue, MultipartFile image);
    void deleteBadge(Long id);

    // ── Student badges ────────────────────────────────────────────────────

    /**
     * Returns all badges earned by the given student, each carrying an
     * {@code earnedAt} timestamp and the badge's {@code triggerValue}.
     */
    List<BadgeResponse> getStudentBadges(Long studentId);

    /**
     * Auto-awards any login-milestone badges the student has not yet earned
     * based on their current {@code loginCount}.
     * Called immediately after {@code loginCount} is incremented.
     */
    void checkAndAwardLoginBadges(Long studentId, int newLoginCount);

    /**
     * Auto-awards any intervention-milestone badges the student has not yet
     * earned based on their current {@code interventionSessionCount}.
     * Called immediately after {@code interventionSessionCount} is incremented.
     */
    void checkAndAwardInterventionBadges(Long studentId, int newSessionCount);

    // ── Achievements ──────────────────────────────────────────────────────
    List<AchievementResponse> getAllAchievements();
    AchievementResponse createAchievement(String title, String description, MultipartFile image);
    AchievementResponse updateAchievement(Long id, String title, String description, MultipartFile image);
    void deleteAchievement(Long id);
}