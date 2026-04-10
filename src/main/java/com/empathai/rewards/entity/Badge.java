package com.empathai.rewards.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;

    @Column(name = "image_type")
    private String imageType;

    // "video" or "module"
    @Column(name = "trigger_type", nullable = false)
    private String triggerType;

    // The title of the video or module that triggers this badge
    @Column(name = "trigger_title", nullable = false)
    private String triggerTitle;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
}