package com.empathai.activities.controller;

import com.empathai.activities.dto.StudentGoalRequest;
import com.empathai.activities.dto.StudentGoalResponse;
import com.empathai.activities.service.IActivityService;
import com.empathai.user.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final IActivityService activityService;

    // ── Save a new goal ───────────────────────────────────────────────────────
    // POST /api/activities/goals
    // Called from frontend Activities → Goal Setting when student adds a goal
    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<StudentGoalResponse>> saveGoal(
            @RequestBody StudentGoalRequest request) {
        StudentGoalResponse response = activityService.saveGoal(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Goal saved successfully."));
    }

    // ── Get all active goals for a student ────────────────────────────────────
    // GET /api/activities/goals/{studentId}
    // Called from frontend when loading Goals and also by RecommendationService
    @GetMapping("/goals/{studentId}")
    public ResponseEntity<ApiResponse<List<StudentGoalResponse>>> getGoals(
            @PathVariable Long studentId) {
        List<StudentGoalResponse> goals = activityService.getGoals(studentId);
        return ResponseEntity.ok(ApiResponse.success(goals, "Goals fetched successfully."));
    }

    // ── Delete a specific goal ────────────────────────────────────────────────
    // DELETE /api/activities/goals/{studentId}/{goalId}
    // Called when student removes a goal from their list
    @DeleteMapping("/goals/{studentId}/{goalId}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable Long studentId,
            @PathVariable Long goalId) {
        activityService.deleteGoal(studentId, goalId);
        return ResponseEntity.ok(ApiResponse.success(null, "Goal deleted successfully."));
    }
}