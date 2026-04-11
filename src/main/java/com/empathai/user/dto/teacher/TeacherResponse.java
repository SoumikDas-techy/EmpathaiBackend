package com.empathai.user.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {

    private Long id;
    private String name;
    private String email;
    private String username;
    private String phoneNumber;
    private boolean active;


    private List<String> subjects;
    private List<String> classesCovered;

    private Long schoolId;
    private String school;
}