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
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}