package com.empathai.rewards.repository;

import com.empathai.rewards.entity.StudentBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentBadgeRepository extends JpaRepository<StudentBadge, Long> {
    List<StudentBadge> findByStudentId(Long studentId);
    boolean existsByStudentIdAndBadgeId(Long studentId, Long badgeId);
    long countByStudentId(Long studentId);
}