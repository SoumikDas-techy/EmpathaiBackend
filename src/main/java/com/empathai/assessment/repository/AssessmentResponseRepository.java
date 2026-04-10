package com.empathai.assessment.repository;

import com.empathai.assessment.entity.AssessmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface AssessmentResponseRepository extends JpaRepository<AssessmentResponse, Long> {
    List<AssessmentResponse> findByGroupName(String groupName);
    List<AssessmentResponse> findByStudentId(String studentId);
    List<AssessmentResponse> findByClassName(String className);
    void deleteByQuestionId(Long questionId);
    List<AssessmentResponse> findByGroupNameIgnoreCaseOrClassNameIgnoreCase(
            String groupName, String className
    );
    List<AssessmentResponse> findByGroupNameIgnoreCaseOrClassNameIgnoreCaseOrSchoolNameIgnoreCase(
            String groupName, String className, String schoolName
    );


    Optional<AssessmentResponse> findByStudentIdAndQuestionId(String studentId, Long questionId);



}