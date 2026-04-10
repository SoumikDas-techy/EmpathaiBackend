package com.empathai.user.repository;

import com.empathai.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // Spring Data JPA will do SELECT s.* FROM students JOIN users ON users.id = students.id
    // This ensures ONLY real Student entities are returned — no ghost base-User records
    List<Student> findAll();

    // Used by SchoolService.getClassesBySchool() and getStudentsBySchoolAndClass()
    List<Student> findBySchoolId(Long schoolId);

    List<Student> findBySchoolIdAndClassName(Long schoolId, String className);

    // Efficient COUNT — used for SchoolSummaryResponse, avoids fetching full rows
    long countBySchoolId(Long schoolId);
}
