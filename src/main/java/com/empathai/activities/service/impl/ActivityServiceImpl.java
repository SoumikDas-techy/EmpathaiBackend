package com.empathai.activities.service.impl;

import com.empathai.activities.dto.StudentGoalRequest;
import com.empathai.activities.dto.StudentGoalResponse;
import com.empathai.activities.entity.StudentGoal;
import com.empathai.activities.repository.StudentGoalRepository;
import com.empathai.activities.service.IActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements IActivityService {

    private final StudentGoalRepository studentGoalRepository;

    // ── Save a new goal ───────────────────────────────────────────────────────
    @Override
    public StudentGoalResponse saveGoal(StudentGoalRequest request) {
        StudentGoal goal = StudentGoal.builder()
                .studentId(request.getStudentId())
                .goalText(request.getGoalText())
                .subjectTag(request.getSubjectTag())
                .build();

        StudentGoal saved = studentGoalRepository.save(goal);
        log.info("Goal saved for studentId={}, subject={}", saved.getStudentId(), saved.getSubjectTag());

        return mapToResponse(saved);
    }

    // ── Get all active goals for a student ────────────────────────────────────
    @Override
    public List<StudentGoalResponse> getGoals(Long studentId) {
        return studentGoalRepository.findByStudentIdAndActiveTrue(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Delete a specific goal ────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteGoal(Long studentId, Long goalId) {
        studentGoalRepository.deleteByStudentIdAndId(studentId, goalId);
        log.info("Goal deleted — studentId={}, goalId={}", studentId, goalId);
    }

    // ── Helper: map entity to response DTO ───────────────────────────────────
    private StudentGoalResponse mapToResponse(StudentGoal goal) {
        return StudentGoalResponse.builder()
                .id(goal.getId())
                .goalText(goal.getGoalText())
                .subjectTag(goal.getSubjectTag())
                .createdAt(goal.getCreatedAt())
                .build();
    }
}