package com.empathai.schedule.controller;

import com.empathai.user.dto.common.ApiResponse;
import com.empathai.schedule.dto.*;
import com.empathai.schedule.service.IScheduleService;
import com.empathai.schedule.service.IRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final IScheduleService scheduleService;
    private final IRecommendationService recommendationService;

    // ── Add a new task ────────────────────────────────────────────────────────
    @PostMapping("/task")
    public ResponseEntity<ApiResponse<TaskResponse>> addTask(@RequestBody TaskRequest request) {
        TaskResponse response = scheduleService.addTask(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Task added successfully."));
    }

    // ── Edit an existing task ─────────────────────────────────────────────────
    @PutMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> editTask(
            @PathVariable Long taskId,
            @RequestBody TaskRequest request) {
        TaskResponse response = scheduleService.editTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Task updated successfully."));
    }

    // ── Toggle task completion ────────────────────────────────────────────────
    @PatchMapping("/task/{taskId}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> toggleComplete(@PathVariable Long taskId) {
        TaskResponse response = scheduleService.toggleComplete(taskId);
        return ResponseEntity.ok(ApiResponse.success(response, "Task completion toggled."));
    }

    // ── Delete a task ─────────────────────────────────────────────────────────
    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {
        scheduleService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(null, "Task deleted successfully."));
    }

    // ── Get all tasks for a student on a specific day ─────────────────────────
    @GetMapping("/{studentId}/{day}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getDayTasks(
            @PathVariable Long studentId,
            @PathVariable String day) {
        List<TaskResponse> tasks = scheduleService.getTasksForDay(studentId, day);
        return ResponseEntity.ok(ApiResponse.success(tasks, "Tasks fetched for " + day));
    }

    // ── Get full week schedule for a student ──────────────────────────────────
    @GetMapping("/{studentId}/week")
    public ResponseEntity<ApiResponse<Map<String, List<TaskResponse>>>> getWeekTasks(
            @PathVariable Long studentId) {
        Map<String, List<TaskResponse>> week = scheduleService.getWeekTasks(studentId);
        return ResponseEntity.ok(ApiResponse.success(week, "Full week schedule fetched."));
    }

    // =========================================================================
    // RECOMMENDATIONS — single call returns blocked times, exams, suggestions
    // =========================================================================

    @GetMapping("/{studentId}/recommendations")
    public ResponseEntity<ApiResponse<ScheduleRecommendationResponse>> getRecommendations(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "Monday") String day) {
        ScheduleRecommendationResponse response = recommendationService.getRecommendations(studentId, day);
        return ResponseEntity.ok(ApiResponse.success(response, "Recommendations fetched."));
    }

    // =========================================================================
    // SCHOOL TIMINGS — admin sets blocked school hours per school
    // =========================================================================

    @PostMapping("/school-timings/{schoolId}")
    public ResponseEntity<ApiResponse<List<SchoolTimingResponse>>> saveSchoolTimings(
            @PathVariable Long schoolId,
            @RequestBody List<SchoolTimingRequest> requests) {
        List<SchoolTimingResponse> saved = recommendationService.saveSchoolTimings(schoolId, requests);
        return ResponseEntity.ok(ApiResponse.success(saved, "School timings saved."));
    }

    @GetMapping("/school-timings/{schoolId}")
    public ResponseEntity<ApiResponse<List<SchoolTimingResponse>>> getSchoolTimings(
            @PathVariable Long schoolId) {
        List<SchoolTimingResponse> timings = recommendationService.getSchoolTimings(schoolId);
        return ResponseEntity.ok(ApiResponse.success(timings, "School timings fetched."));
    }

    // =========================================================================
    // EXAM DATES — admin adds upcoming exam dates
    // =========================================================================

    @PostMapping("/exam-dates")
    public ResponseEntity<ApiResponse<ExamDateResponse>> addExamDate(
            @RequestBody ExamDateRequest request) {
        ExamDateResponse response = recommendationService.saveExamDate(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Exam date added."));
    }

    @GetMapping("/exam-dates/{schoolId}")
    public ResponseEntity<ApiResponse<List<ExamDateResponse>>> getExamDates(
            @PathVariable Long schoolId) {
        List<ExamDateResponse> exams = recommendationService.getExamDatesBySchool(schoolId);
        return ResponseEntity.ok(ApiResponse.success(exams, "Exam dates fetched."));
    }

    @DeleteMapping("/exam-dates/{examId}")
    public ResponseEntity<ApiResponse<Void>> deleteExamDate(@PathVariable Long examId) {
        recommendationService.deleteExamDate(examId);
        return ResponseEntity.ok(ApiResponse.success(null, "Exam date deleted."));
    }

}