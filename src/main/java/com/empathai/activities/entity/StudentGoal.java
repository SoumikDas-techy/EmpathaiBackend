package com.empathai.activities.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "goal_text", nullable = false)
    private String goalText;

    // e.g. "Mathematics", "Science", "English", "Hindi", "SST"
    @Column(name = "subject_tag", nullable = false)
    private String subjectTag;

    // Target date the student wants to achieve the goal by
    @Column(name = "target_date")
    private LocalDate targetDate;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean active = true;
}