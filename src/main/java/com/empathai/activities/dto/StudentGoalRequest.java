package com.empathai.activities.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGoalRequest {

    private Long studentId;
    private String goalText;
    private String subjectTag; // e.g. "Mathematics", "Science", "English", "Hindi", "SST"
}