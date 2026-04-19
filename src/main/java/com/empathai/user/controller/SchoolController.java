package com.empathai.user.controller;

import com.empathai.user.dto.school.ClassSummaryResponse;
import com.empathai.user.dto.school.SchoolRequest;
import com.empathai.user.dto.school.SchoolResponse;
import com.empathai.user.dto.school.SchoolSummaryResponse;
import com.empathai.user.dto.user.StudentDetailResponse;
import com.empathai.user.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;
    private static final Logger logger = LoggerFactory.getLogger(SchoolController.class);


    @PostMapping
    public ResponseEntity<SchoolResponse> createSchool(@Valid @RequestBody SchoolRequest request) {
        logger.info("createSchool started");
        try {
            ResponseEntity<SchoolResponse> response = new ResponseEntity<>(schoolService.createSchool(request), HttpStatus.CREATED);
            logger.info("createSchool completed successfully");
            return response;
        } catch (Exception e) {
            logger.error("createSchool failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<SchoolResponse>> getAllSchools() {
        logger.info("getAllSchools started");
        try {
            ResponseEntity<List<SchoolResponse>> response = ResponseEntity.ok(schoolService.getAllSchools());
            logger.info("getAllSchools completed successfully");
            return response;
        } catch (Exception e) {
            logger.error("getAllSchools failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getSchoolsSummary() {
        logger.info("getSchoolsSummary started");
        try {
            List<Map<String, Object>> summary = schoolService.getAllSchools().stream()
                    .map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName()))
                    .toList();
            ResponseEntity<List<Map<String, Object>>> response = ResponseEntity.ok(summary);
            logger.info("getSchoolsSummary completed successfully");
            return response;
        } catch (Exception e) {
            logger.error("getSchoolsSummary failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolResponse> getSchoolById(@PathVariable Long id) {
        logger.info("getSchoolById started");
        try {
            ResponseEntity<SchoolResponse> response = ResponseEntity.ok(schoolService.getSchoolById(id));
            logger.info("getSchoolById completed successfully");
            return response;
        } catch (Exception e) {
            logger.error("getSchoolById failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolResponse> updateSchool(@PathVariable Long id,
                                                       @Valid @RequestBody SchoolRequest request) {
        logger.info("updateSchool started");
        try {
            ResponseEntity<SchoolResponse> response = ResponseEntity.ok(schoolService.updateSchool(id, request));
            logger.info("updateSchool completed successfully");
            return response;
        } catch (Exception e) {
            logger.error("updateSchool failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long id) {
        logger.info("deleteSchool started");
        try {
            schoolService.deleteSchool(id);
            ResponseEntity<Void> response = ResponseEntity.noContent().build();
            logger.info("deleteSchool completed successfully");
            return response;
        } catch (Exception e) {
            logger.error("deleteSchool failed: " + e.getMessage(), e);
            throw e;
        }
    }






    /**
     * GET /api/schools/{id}/classes
     * LEVEL 2 - Classes inside a school: className, studentCount only.
     * Frontend calls this when user drills into a school.
     */
    @GetMapping("/{id}/classes")
    public ResponseEntity<List<ClassSummaryResponse>> getClasses(@PathVariable Long id) {
        logger.info("getClasses started for schoolId={}", id);
        try {
            ResponseEntity<List<ClassSummaryResponse>> response = ResponseEntity.ok(schoolService.getClassesBySchool(id));
            logger.info("getClasses completed successfully for schoolId={}", id);
            return response;
        } catch (Exception e) {
            logger.error("getClasses failed for schoolId={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /api/schools/{id}/classes/{className}/students
     * LEVEL 3 - Full student detail inside a class. No audit fields.
     * Frontend calls this only when user drills into a specific class.
     */
    @GetMapping("/{id}/classes/{className}/students")
    public ResponseEntity<List<StudentDetailResponse>> getStudentsInClass(
            @PathVariable Long id,
            @PathVariable String className) {
        logger.info("getStudentsInClass started for schoolId={}, className={}", id, className);
        try {
            ResponseEntity<List<StudentDetailResponse>> response = ResponseEntity.ok(schoolService.getStudentsBySchoolAndClass(id, className));
            logger.info("getStudentsInClass completed successfully for schoolId={}, className={}", id, className);
            return response;
        } catch (Exception e) {
            logger.error("getStudentsInClass failed for schoolId={}, className={}: {}", id, className, e.getMessage(), e);
            throw e;
        }
    }



}
