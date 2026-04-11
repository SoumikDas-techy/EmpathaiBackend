package com.empathai.rewards.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BadgeResponse {
    private Long id;
    private String title;
    private String imageBase64;   // base64 encoded image for frontend
    private String imageType;
    private String triggerType;
    private String triggerTitle;
    private String triggerValue;  // milestone number for login/intervention badges
    private String description;
    /** Populated only when returning a student's earned badge (GET /students/{id}/badges). */
    private LocalDateTime earnedAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}