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

    // ── Achievements ──────────────────────────────────────────────────────
    List<AchievementResponse> getAllAchievements();
    AchievementResponse createAchievement(String title, String description, MultipartFile image);
    AchievementResponse updateAchievement(Long id, String title, String description, MultipartFile image);
    void deleteAchievement(Long id);
}