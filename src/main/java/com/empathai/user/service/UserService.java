package com.empathai.user.service;

import com.empathai.user.dto.user.*;
import com.empathai.user.entity.*;
import com.empathai.user.entity.enums.UserRole;
import com.empathai.user.exception.EmpathaiException;
import com.empathai.user.repository.SchoolRepository;
import com.empathai.user.repository.StudentRepository;
import com.empathai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────────────────────
    // CREATE / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            request = UserRequest.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .username(request.getEmail())
                    .password(request.getPassword())
                    .role(request.getRole())
                    .schoolId(resolveSchoolId(request))
                    .school(request.getSchool())
                    .className(request.getClassName())
                    .section(request.getSection())
                    .bloodGroup(request.getBloodGroup())
                    .phoneNumber(request.getPhoneNumber())
                    .parentEmail(request.getParentEmail())
                    .rollNo(request.getRollNo())
                    .dateOfBirth(request.getDateOfBirth())
                    .parentName(request.getParentName())
                    .address(request.getAddress())
                    .build();
        }

        Long schoolId = resolveSchoolId(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmpathaiException("Email already exists", "DUPLICATE_EMAIL");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new EmpathaiException("Username already exists", "DUPLICATE_USERNAME");
        }

        User user;
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        switch (request.getRole()) {
            case SUPER_ADMIN -> user = new SuperAdmin(request.getEmail(), encodedPassword, request.getName());
            case CONTENT_ADMIN -> {
                ContentAdmin ca = new ContentAdmin(request.getEmail(), encodedPassword, request.getName());
                if (request.getPhoneNumber() != null) ca.setPhoneNumber(request.getPhoneNumber());
                user = ca;
            }
            case PSYCHOLOGIST -> {
                Psychologist p = new Psychologist(request.getEmail(), encodedPassword, request.getName());
                if (request.getPhoneNumber() != null) p.setPhoneNumber(request.getPhoneNumber());
                user = p;
            }
            case SCHOOL_ADMIN -> {
                SchoolAdmin sa = new SchoolAdmin(request.getEmail(), encodedPassword, request.getName());
                sa.setSchoolId(schoolId);
                user = sa;
            }
            case STUDENT -> {
                Student s = new Student(request.getEmail(), encodedPassword, request.getName());
                s.setSchoolId(schoolId);
                s.setClassName(request.getClassName());
                s.setSection(request.getSection());
                s.setPhoneNumber(request.getPhoneNumber());
                s.setParentEmail(request.getParentEmail());
                s.setRollNo(request.getRollNo());
                s.setDateOfBirth(request.getDateOfBirth());

                if (request.getAge() != null) s.setAge(request.getAge());
                s.setParentName(request.getParentName());
                if (request.getGender() != null) s.setGender(request.getGender());
                user = s;
            }
            default -> throw new EmpathaiException("Invalid role provided");
        }

        user.setUsername(request.getUsername());
        return mapToFullResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EmpathaiException("User not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank())
            user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().isBlank())
            user.setPassword(passwordEncoder.encode(request.getPassword()));

        Long schoolId = resolveSchoolId(request);

        if (user instanceof Student s) {
            if (request.getRollNo() != null) s.setRollNo(request.getRollNo());
            if (request.getClassName() != null) s.setClassName(request.getClassName());
            if (schoolId != null) s.setSchoolId(schoolId);
            if (request.getSection() != null) s.setSection(request.getSection());
            if (request.getPhoneNumber() != null) s.setPhoneNumber(request.getPhoneNumber());
            if (request.getParentEmail() != null) s.setParentEmail(request.getParentEmail());
            if (request.getDateOfBirth() != null) s.setDateOfBirth(request.getDateOfBirth());
            if (request.getAge() != null) s.setAge(request.getAge());
            if (request.getParentName() != null) s.setParentName(request.getParentName());
            if (request.getGender() != null) s.setGender(request.getGender());
        } else if (user instanceof Psychologist p) {
            if (request.getPhoneNumber() != null) p.setPhoneNumber(request.getPhoneNumber());
        } else if (user instanceof ContentAdmin ca) {
            if (request.getPhoneNumber() != null) ca.setPhoneNumber(request.getPhoneNumber());
        } else if (user instanceof SchoolAdmin sa) {
            if (schoolId != null) sa.setSchoolId(schoolId);
        }

        return mapToFullResponse(userRepository.save(user));
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EmpathaiException("User not found with id: " + id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EmpathaiException("User not found");
        }
        userRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // ROLE-SPECIFIC LIST ENDPOINTS (lean DTOs, no audit fields)
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/users/students
     * Returns StudentSummaryResponse: id, name, email, username, active, school, className, rollNo.
     * No audit fields. No student-only fields irrelevant to list view.
     */
    public Page<StudentSummaryResponse> getStudentPage(String school, String search, int page, int size) {
        List<StudentSummaryResponse> all = studentRepository.findAll().stream()
                .filter(s -> {
                    if (school == null) return true;
                    String schoolName = s.getSchoolId() != null
                            ? schoolRepository.findById(s.getSchoolId()).map(sc -> sc.getName()).orElse("")
                            : "";
                    return school.equals(schoolName);
                })
                .filter(s -> search == null
                        || (s.getName() != null && s.getName().toLowerCase().contains(search.toLowerCase()))
                        || (s.getEmail() != null && s.getEmail().toLowerCase().contains(search.toLowerCase())))
                .map(s -> StudentSummaryResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .email(s.getEmail())
                        .username(s.getUsername())
                        .active(Boolean.TRUE.equals(s.getActive()))
                        .className(s.getClassName())
                        .rollNo(s.getRollNo())
                        // school name resolved from schoolId
                        .school(s.getSchoolId() != null
                                ? schoolRepository.findById(s.getSchoolId()).map(sc -> sc.getName()).orElse(null)
                                : null)
                        .build())
                .collect(Collectors.toList());

        int start = Math.min(page * size, all.size());
        int end = Math.min(start + size, all.size());
        return new PageImpl<>(all.subList(start, end), PageRequest.of(page, size), all.size());
    }

    /**
     * GET /api/users/school-admins
     * Returns SchoolAdminResponse: id, name, email, username, active, schoolId, school name.
     * No audit fields. No student-specific fields.
     */
    public Page<SchoolAdminResponse> getSchoolAdminPage(String search, int page, int size) {
        List<SchoolAdminResponse> all = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.SCHOOL_ADMIN)
                .filter(u -> search == null
                        || (u.getName() != null && u.getName().toLowerCase().contains(search.toLowerCase())))
                .map(u -> {
                    SchoolAdmin sa = (SchoolAdmin) u;
                    SchoolAdminResponse.SchoolAdminResponseBuilder b = SchoolAdminResponse.builder()
                            .id(sa.getId())
                            .name(sa.getName())
                            .email(sa.getEmail())
                            .username(sa.getUsername())
                            .active(Boolean.TRUE.equals(sa.getActive()))
                            .schoolId(sa.getSchoolId());
                    if (sa.getSchoolId() != null) {
                        schoolRepository.findById(sa.getSchoolId())
                                .ifPresent(s -> b.school(s.getName()));
                    }
                    return b.build();
                })
                .collect(Collectors.toList());

        int start = Math.min(page * size, all.size());
        int end = Math.min(start + size, all.size());
        return new PageImpl<>(all.subList(start, end), PageRequest.of(page, size), all.size());
    }

    /**
     * GET /api/users/psychologists
     * Returns PsychologistResponse: id, name, email, username, phoneNumber, active.
     * No audit fields. No school/student fields.
     */
    public Page<PsychologistResponse> getPsychologistPage(String search, int page, int size) {
        List<PsychologistResponse> all = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.PSYCHOLOGIST)
                .filter(u -> search == null
                        || (u.getName() != null && u.getName().toLowerCase().contains(search.toLowerCase())))
                .map(u -> {
                    Psychologist p = (Psychologist) u;
                    return PsychologistResponse.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .email(p.getEmail())
                            .username(p.getUsername())
                            .phoneNumber(p.getPhoneNumber())
                            .active(Boolean.TRUE.equals(p.getActive()))
                            .build();
                })
                .collect(Collectors.toList());

        int start = Math.min(page * size, all.size());
        int end = Math.min(start + size, all.size());
        return new PageImpl<>(all.subList(start, end), PageRequest.of(page, size), all.size());
    }

    /**
     * GET /api/users/content-admins
     * Returns ContentAdminResponse: id, name, email, username, phoneNumber, active.
     * No audit fields. No school/student fields.
     */
    public Page<ContentAdminResponse> getContentAdminPage(String search, int page, int size) {
        List<ContentAdminResponse> all = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.CONTENT_ADMIN)
                .filter(u -> search == null
                        || (u.getName() != null && u.getName().toLowerCase().contains(search.toLowerCase())))
                .map(u -> {
                    ContentAdmin ca = (ContentAdmin) u;
                    return ContentAdminResponse.builder()
                            .id(ca.getId())
                            .name(ca.getName())
                            .email(ca.getEmail())
                            .username(ca.getUsername())
                            .phoneNumber(ca.getPhoneNumber())
                            .active(Boolean.TRUE.equals(ca.getActive()))
                            .build();
                })
                .collect(Collectors.toList());

        int start = Math.min(page * size, all.size());
        int end = Math.min(start + size, all.size());
        return new PageImpl<>(all.subList(start, end), PageRequest.of(page, size), all.size());
    }

    // ─────────────────────────────────────────────────────────────
    // DETAIL / INTERNAL USE — keeps full UserResponse
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/users/{id} — full user detail for edit screens.
     * Keeps all fields. Audit fields excluded from UserResponse DTO itself.
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EmpathaiException("User not found with id: " + id));
        return mapToFullResponse(user);
    }

    /** Used by /me endpoint and internal calls. */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToFullResponse)
                .collect(Collectors.toList());
    }

    /** Still available for internal use but no longer exposed on list endpoints. */
    public List<UserResponse> getUsersByRole(UserRole role) {
        if (role == UserRole.STUDENT) {
            return studentRepository.findAll().stream()
                    .map(this::mapToFullResponse)
                    .collect(Collectors.toList());
        }
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(this::mapToFullResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // INTERNAL HELPERS
    // ─────────────────────────────────────────────────────────────

    private Long resolveSchoolId(UserRequest request) {
        if (request.getSchoolId() != null) {
            return request.getSchoolId();
        }
        if (request.getSchool() != null && !request.getSchool().isBlank()) {
            return schoolRepository.findByName(request.getSchool())
                    .map(s -> s.getId())
                    .orElse(null);
        }
        return null;
    }

    /**
     * Full response mapper — used for create/update/detail views only.
     * Audit fields are excluded from UserResponse DTO (not mapped here).
     */
    public UserResponse mapToFullResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .active(Boolean.TRUE.equals(user.getActive()));
        // Audit fields intentionally not mapped — removed from UserResponse DTO

        if (user instanceof SchoolAdmin sa && sa.getSchoolId() != null) {
            builder.schoolId(sa.getSchoolId());
            schoolRepository.findById(sa.getSchoolId())
                    .ifPresent(s -> builder.school(s.getName()));
        } else if (user instanceof Psychologist p) {
            builder.phoneNumber(p.getPhoneNumber());
        } else if (user instanceof ContentAdmin ca) {
            builder.phoneNumber(ca.getPhoneNumber());
        } else if (user instanceof Student s && s.getSchoolId() != null) {
            builder.schoolId(s.getSchoolId())
                    .rollNo(s.getRollNo())
                    .className(s.getClassName())
                    .section(s.getSection())
                    .phoneNumber(s.getPhoneNumber())
                    .parentEmail(s.getParentEmail())
                    .dateOfBirth(s.getDateOfBirth())
                    .age(s.getAge())
                    .gender(s.getGender())
                    .parentName(s.getParentName());
            schoolRepository.findById(s.getSchoolId())
                    .ifPresent(sc -> builder.school(sc.getName()));
        }


        return builder.build();
    }
}
