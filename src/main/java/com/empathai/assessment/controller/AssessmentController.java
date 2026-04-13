package com.empathai.assessment.controller;

import com.empathai.assessment.dto.*;
import com.empathai.assessment.service.IAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssessmentController {

    private final IAssessmentService assessmentService;

    // ── Groups ────────────────────────────────────────────────────────────────

    @GetMapping("/groups")
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        return ResponseEntity.ok(assessmentService.getAllGroups());
    }

    @GetMapping("/groups/class/{className}")
    public ResponseEntity<List<GroupResponse>> getGroupsByClass(
            @PathVariable String className) {
        return ResponseEntity.ok(assessmentService.getGroupsByClassName(className));
    }

    @PostMapping("/groups")
    public ResponseEntity<GroupResponse> createGroup(@RequestBody GroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.createGroup(request));
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        assessmentService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // ── Questions ─────────────────────────────────────────────────────────────

    @GetMapping("/questions")
    public ResponseEntity<Page<QuestionResponse>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(assessmentService.getQuestions(page, size));
    }
    @GetMapping("/questions/class/{className}")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByClass(
            @PathVariable String className) {

        List<QuestionResponse> combinedQuestions = new ArrayList<>();

        // Only fetch questions for the student's own class group
        List<GroupResponse> classGroups = assessmentService.getGroupsByClassName(className);
        for (GroupResponse group : classGroups) {
            List<QuestionResponse> qs = assessmentService.getQuestionsByGroupMap(group.getId());
            if (qs != null) combinedQuestions.addAll(qs);
        }

        // Deduplicate by question ID
        List<QuestionResponse> finalQuestions = combinedQuestions.stream()
                .filter(q -> q.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                QuestionResponse::getId,
                                q -> q,
                                (a, b) -> a,
                                java.util.LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        return ResponseEntity.ok(finalQuestions);
    }
    /** Fetch responses by student class name (for admin response sheet) */
    @GetMapping("/responses/by-class/{className}")
    public ResponseEntity<List<ResponseDto>> getResponsesByClass(
            @PathVariable String className) {
        return ResponseEntity.ok(assessmentService.getResponsesByGroup(className));
    }

    @GetMapping("/questions/group/{groupId}")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByGroup(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(assessmentService.getQuestionsByGroupMap(groupId));
    }

    @PostMapping("/questions")
    public ResponseEntity<QuestionResponse> createQuestion(@RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.createQuestion(request));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id, @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(assessmentService.updateQuestion(id, request));
    }

    @DeleteMapping("/questions/{id}")

    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        assessmentService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }


    // ── Responses ─────────────────────────────────────────────────────────────

    @GetMapping("/responses")
    public ResponseEntity<Page<ResponseDto>> getResponses(
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size) {
        return ResponseEntity.ok(assessmentService.getResponses(studentId, page, size));
    }

    @GetMapping("/responses/group/{groupName}")
    public ResponseEntity<List<ResponseDto>> getResponsesByGroup(@PathVariable String groupName) {
        return ResponseEntity.ok(assessmentService.getResponsesByGroup(groupName));
    }

    @GetMapping("/responses/sheet/{groupName}")
    public ResponseEntity<List<ResponseDto>> getResponseSheet(@PathVariable String groupName) {
        return ResponseEntity.ok(assessmentService.getResponsesByGroup(groupName));
    }

    @PostMapping("/responses")
    public ResponseEntity<ResponseDto> createResponse(@RequestBody ResponseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.createResponse(request));
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, Object>> getAnalyticsSummary(
            @RequestParam(defaultValue = "ALL") String filter) {
        return ResponseEntity.ok(assessmentService.getAnalyticsSummary(filter));
    }

    @GetMapping("/analytics/group/{groupName}")
    public ResponseEntity<Map<String, Object>> getGroupAnalytics(
            @PathVariable String groupName,
            @RequestParam(defaultValue = "ALL") String filter) {
        return ResponseEntity.ok(assessmentService.getGroupAnalytics(groupName, filter));
    }
    @PostMapping("/api/test-responses")
    public ResponseEntity<String> testEndpoint(@RequestBody(required = false) String body) {
        return ResponseEntity.ok("Works! Body: " + body);
    }
}