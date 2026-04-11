package com.empathai.assessment.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseRequest {
    private String studentId;
    private String studentName;
    private Long questionId;
    private String questionText;
    private String responseValue;
    private String answer;
    private String emotion;
    private String groupId;
    private String groupName;
    private String className;
    private String schoolName;
    private String gender;
    private Integer age;
}