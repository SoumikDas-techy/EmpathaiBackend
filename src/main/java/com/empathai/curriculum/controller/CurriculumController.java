package com.empathai.curriculum.controller;

import com.empathai.curriculum.dto.request.ModuleRequest;
import com.empathai.curriculum.dto.request.QuizQuestionRequest;
import com.empathai.curriculum.dto.request.SubTopicRequest;
import com.empathai.curriculum.dto.request.SyllabusRequest;
import com.empathai.curriculum.dto.response.ModuleResponse;
import com.empathai.curriculum.dto.response.QuizQuestionResponse;
import com.empathai.curriculum.dto.response.SubTopicResponse;
import com.empathai.curriculum.dto.response.SyllabusResponse;
import com.empathai.curriculum.service.CurriculumService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/curriculum")
@Validated
public class CurriculumController {

    private final CurriculumService curriculumService;

    public CurriculumController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    private String normalizeText(String text) {
        if (text == null) return null;
        return text.replace("\r\n", "\n").replace("\r", "\n").trim();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SYLLABUS
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/syllabi")
    public ResponseEntity<List<SyllabusResponse>> getAllSyllabi() {
        return ResponseEntity.ok(curriculumService.getAllSyllabi());
    }

    @GetMapping("/syllabi/class/{classLevel}")
    public ResponseEntity<List<SyllabusResponse>> getSyllabiByClass(
            @PathVariable String classLevel) {
        return ResponseEntity.ok(curriculumService.getSyllabiByClassLevel(classLevel));
    }

    @PostMapping("/syllabi")
    public ResponseEntity<SyllabusResponse> createSyllabus(
            @Valid @RequestBody SyllabusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(curriculumService.createSyllabus(request));
    }

    @PutMapping("/syllabi/{id}")
    public ResponseEntity<SyllabusResponse> updateSyllabus(
            @PathVariable Long id,
            @Valid @RequestBody SyllabusRequest request) {
        return ResponseEntity.ok(curriculumService.updateSyllabus(id, request));
    }

    @DeleteMapping("/syllabi/{id}")
    public ResponseEntity<Map<String, String>> deleteSyllabus(@PathVariable Long id) {
        curriculumService.deleteSyllabus(id);
        return ResponseEntity.ok(Map.of("message", "Syllabus deleted successfully"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MODULE  (title only — no content fields)
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/modules/syllabus/{syllabusId}")
    public ResponseEntity<List<ModuleResponse>> getModules(@PathVariable Long syllabusId) {
        return ResponseEntity.ok(curriculumService.getModulesBySyllabus(syllabusId));
    }

    @PostMapping("/modules")
    public ResponseEntity<ModuleResponse> createModule(
            @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(curriculumService.createModule(request));
    }

    @PutMapping("/modules/{id}")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(curriculumService.updateModule(id, request));
    }

    @DeleteMapping("/modules/{id}")
    public ResponseEntity<Map<String, String>> deleteModule(@PathVariable Long id) {
        curriculumService.deleteModule(id);
        return ResponseEntity.ok(Map.of("message", "Module deleted successfully"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SUBTOPIC
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/subtopics/module/{moduleId}")
    public ResponseEntity<List<SubTopicResponse>> getSubTopics(@PathVariable Long moduleId) {
        return ResponseEntity.ok(curriculumService.getSubTopicsByModule(moduleId));
    }

    @PostMapping(value = "/subtopics", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubTopicResponse> createSubTopic(
            @RequestParam @NotNull(message = "Module ID must not be null") Long moduleId,
            @RequestParam @NotBlank(message = "SubTopic title must not be blank") String title,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String learningObjectives,
            @RequestParam(required = false) Integer orderIndex,
            @RequestParam(required = false) MultipartFile summaryImage,
            @RequestParam(required = false) String createdBy) {

        SubTopicRequest request = new SubTopicRequest(
                moduleId,
                normalizeText(title),
                normalizeText(videoUrl),
                normalizeText(summary),
                normalizeText(learningObjectives),
                orderIndex,
                createdBy, null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(curriculumService.createSubTopic(request, summaryImage));
    }

    @PutMapping(value = "/subtopics/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubTopicResponse> updateSubTopic(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "Module ID must not be null") Long moduleId,
            @RequestParam @NotBlank(message = "SubTopic title must not be blank") String title,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String learningObjectives,
            @RequestParam(required = false) Integer orderIndex,
            @RequestParam(required = false) MultipartFile summaryImage,
            @RequestParam(required = false) String modifiedBy) {

        SubTopicRequest request = new SubTopicRequest(
                moduleId,
                normalizeText(title),
                normalizeText(videoUrl),
                normalizeText(summary),
                normalizeText(learningObjectives),
                orderIndex,
                null, modifiedBy);

        return ResponseEntity.ok(curriculumService.updateSubTopic(id, request, summaryImage));
    }

    @DeleteMapping("/subtopics/{id}")
    public ResponseEntity<Map<String, String>> deleteSubTopic(@PathVariable Long id) {
        curriculumService.deleteSubTopic(id);
        return ResponseEntity.ok(Map.of("message", "SubTopic deleted successfully"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // QUIZ
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/quiz/subtopic/{subTopicId}")
    public ResponseEntity<List<QuizQuestionResponse>> getQuiz(@PathVariable Long subTopicId) {
        return ResponseEntity.ok(curriculumService.getQuizBySubTopic(subTopicId));
    }

    @PostMapping(value = "/quiz", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuizQuestionResponse> createQuiz(
            @RequestParam @NotNull(message = "SubTopic ID must not be null") Long subTopicId,
            @RequestParam @NotBlank(message = "Question text must not be blank") String questionText,
            @RequestParam @NotBlank(message = "Option A must not be blank") String optionA,
            @RequestParam @NotBlank(message = "Option B must not be blank") String optionB,
            @RequestParam @NotBlank(message = "Option C must not be blank") String optionC,
            @RequestParam @NotBlank(message = "Option D must not be blank") String optionD,
            @RequestParam @NotNull(message = "Correct answer must not be null") Integer correctAnswer,
            @RequestParam(required = false) String explanation,
            @RequestParam(required = false) MultipartFile questionImage,
            @RequestParam(required = false) String createdBy) {

        QuizQuestionRequest request = new QuizQuestionRequest(
                subTopicId, questionText, optionA, optionB, optionC, optionD,
                correctAnswer, explanation, createdBy, null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(curriculumService.createQuizQuestion(request, questionImage));
    }

    @PutMapping(value = "/quiz/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuizQuestionResponse> updateQuiz(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "SubTopic ID must not be null") Long subTopicId,
            @RequestParam @NotBlank(message = "Question text must not be blank") String questionText,
            @RequestParam @NotBlank(message = "Option A must not be blank") String optionA,
            @RequestParam @NotBlank(message = "Option B must not be blank") String optionB,
            @RequestParam @NotBlank(message = "Option C must not be blank") String optionC,
            @RequestParam @NotBlank(message = "Option D must not be blank") String optionD,
            @RequestParam @NotNull(message = "Correct answer must not be null") Integer correctAnswer,
            @RequestParam(required = false) String explanation,
            @RequestParam(required = false) MultipartFile questionImage,
            @RequestParam(required = false) String modifiedBy) {

        QuizQuestionRequest request = new QuizQuestionRequest(
                subTopicId, questionText, optionA, optionB, optionC, optionD,
                correctAnswer, explanation, null, modifiedBy);

        return ResponseEntity.ok(curriculumService.updateQuizQuestion(id, request, questionImage));
    }

    @DeleteMapping("/quiz/{id}")
    public ResponseEntity<Map<String, String>> deleteQuiz(@PathVariable Long id) {
        curriculumService.deleteQuizQuestion(id);
        return ResponseEntity.ok(Map.of("message", "Quiz question deleted successfully"));
    }
}