package com.empathai.schedule.service.impl;

import com.empathai.activities.entity.StudentGoal;
import com.empathai.activities.repository.StudentGoalRepository;
import com.empathai.schedule.dto.*;
import com.empathai.schedule.entity.ExamDate;
import com.empathai.schedule.entity.SchoolTiming;
import com.empathai.schedule.repository.ExamDateRepository;
import com.empathai.schedule.repository.SchoolTimingRepository;
import com.empathai.schedule.repository.ScheduleTaskRepository;
import com.empathai.schedule.entity.ScheduleTask;
import com.empathai.schedule.service.IRecommendationService;
import com.empathai.user.entity.Student;
import com.empathai.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements IRecommendationService {

    private final ExamDateRepository examDateRepository;
    private final StudentGoalRepository studentGoalRepository;
    private final SchoolTimingRepository schoolTimingRepository;
    private final ScheduleTaskRepository scheduleTaskRepository;
    private final StudentRepository studentRepository;

    // ── Weekly fallback subjects confirmed by sir ─────────────────────────────
    private static final List<String> WEEKLY_SUBJECTS = List.of(
            "Mathematics", "Science", "SST", "English", "Hindi"
    );

    // ── Default estimated session duration in minutes ─────────────────────────
    private static final int DEFAULT_SESSION_MINS = 45;

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN METHOD — called by controller on schedule load
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public ScheduleRecommendationResponse getRecommendations(Long studentId, String dayOfWeek) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        Long schoolId = student.getSchoolId();
        String className = student.getClassName();

        // ═══════════════════════════════════════════════════════════════════════
        // DEBUG LOGGING START
        // ═══════════════════════════════════════════════════════════════════════
        log.info("═══════════════════════════════════════════════════════════");
        log.info("🔍 RECOMMENDATIONS REQUEST");
        log.info("   Student ID: {}", studentId);
        log.info("   Class Name: '{}'", className);
        log.info("   School ID: {}", schoolId);
        log.info("   Day: {}", dayOfWeek);
        log.info("═══════════════════════════════════════════════════════════");

        // 1. Blocked windows for this day — filtered by student's class
        List<SchoolTimingResponse> blockedWindows = getBlockedWindows(schoolId, dayOfWeek, className);
        log.info("📚 BLOCKED WINDOWS: {} found for {} in class '{}'", blockedWindows.size(), dayOfWeek, className);
        blockedWindows.forEach(w -> log.info("   - {} to {}", w.getStartTime(), w.getEndTime()));

        // 2. Upcoming exams for this student
        List<ExamDateResponse> upcomingExams = getUpcomingExams(schoolId, className);
        log.info("📝 UPCOMING EXAMS: {} found for class '{}'", upcomingExams.size(), className);
        if (upcomingExams.isEmpty()) {
            log.warn("   ⚠️  NO EXAMS FOUND - Check database for:");
            log.warn("       - schoolId = {}", schoolId);
            log.warn("       - className = '{}'", className);
            log.warn("       - examDate > {}", LocalDate.now());
        } else {
            upcomingExams.forEach(e -> log.info("   - {} exam on {} ({} days, urgency: {})",
                    e.getSubjectName(), e.getExamDate(), e.getDaysRemaining(), e.getUrgency()));
        }

        // 3. Student's active goals — read from activities package
        List<StudentGoal> goals = studentGoalRepository.findByStudentIdAndActiveTrue(studentId);
        Set<String> goalSubjects = goals.stream()
                .map(StudentGoal::getSubjectTag)
                .collect(Collectors.toSet());
        log.info("🎯 ACTIVE GOALS: {} goals found", goalSubjects.size());
        goalSubjects.forEach(g -> log.info("   - {}", g));

        // 4. Check which weekly subjects the student has already covered this week
        List<ScheduleTask> weekTasks = scheduleTaskRepository.findByStudentId(studentId);
        Set<String> coveredSubjects = WEEKLY_SUBJECTS.stream()
                .filter(subject -> weekTasks.stream()
                        .anyMatch(t -> t.getTitle() != null &&
                                t.getTitle().toLowerCase().contains(subject.toLowerCase())))
                .collect(Collectors.toSet());
        log.info("✅ COVERED THIS WEEK: {} subjects", coveredSubjects.size());
        coveredSubjects.forEach(s -> log.info("   - {}", s));

        // 5. Generate and rank suggestions
        List<TaskSuggestion> suggestions = generateSuggestions(upcomingExams, goalSubjects, coveredSubjects);
        log.info("💡 SUGGESTIONS GENERATED: {} (before today's filter)", suggestions.size());
        suggestions.forEach(s -> log.info("   - {} (score: {}, reason: '{}')",
                s.getTitle(), s.getScore(), s.getReasonLabel()));

        // 6. Remove any suggestion whose subject is already scheduled TODAY
        List<ScheduleTask> todayTasks = scheduleTaskRepository.findByStudentIdAndDayOfWeek(studentId, dayOfWeek);
        Set<String> todaySubjects = todayTasks.stream()
                .filter(t -> t.getTitle() != null)
                .flatMap(t -> WEEKLY_SUBJECTS.stream()
                        .filter(s -> t.getTitle().toLowerCase().contains(s.toLowerCase())))
                .collect(Collectors.toSet());
        // Also check goal subjects covered today
        goalSubjects.forEach(gs -> todayTasks.stream()
                .filter(t -> t.getTitle() != null && t.getTitle().toLowerCase().contains(gs.toLowerCase()))
                .findFirst()
                .ifPresent(t -> todaySubjects.add(gs)));

        log.info("🚫 ALREADY SCHEDULED TODAY ({}): {} subjects", dayOfWeek, todaySubjects.size());
        todaySubjects.forEach(s -> log.info("   - {}", s));

        suggestions = suggestions.stream()
                .filter(s -> !todaySubjects.contains(s.getSubjectName()))
                .collect(Collectors.toList());

        log.info("✨ FINAL SUGGESTIONS: {} (after filtering today's tasks)", suggestions.size());
        suggestions.forEach(s -> log.info("   - {} (reason: '{}')", s.getTitle(), s.getReasonLabel()));
        log.info("═══════════════════════════════════════════════════════════");
        // ═══════════════════════════════════════════════════════════════════════
        // DEBUG LOGGING END
        // ═══════════════════════════════════════════════════════════════════════

        return ScheduleRecommendationResponse.builder()
                .blockedWindows(blockedWindows)
                .upcomingExams(upcomingExams)
                .suggestions(suggestions)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BLOCKED WINDOWS
    // ─────────────────────────────────────────────────────────────────────────

    private List<SchoolTimingResponse> getBlockedWindows(Long schoolId, String dayOfWeek, String className) {
        if (schoolId == null || className == null) return Collections.emptyList();

        return schoolTimingRepository.findBySchoolId(schoolId).stream()
                .filter(t -> t.getDayOfWeek().equalsIgnoreCase(dayOfWeek))
                .filter(t -> t.getClassName() != null && t.getClassName().equalsIgnoreCase(className))
                .map(t -> SchoolTimingResponse.builder()
                        .id(t.getId())
                        .className(t.getClassName())
                        .dayOfWeek(t.getDayOfWeek())
                        .startTime(t.getStartTime())
                        .endTime(t.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPCOMING EXAMS
    // ─────────────────────────────────────────────────────────────────────────

    private List<ExamDateResponse> getUpcomingExams(Long schoolId, String className) {
        if (schoolId == null || className == null) return Collections.emptyList();

        LocalDate today = LocalDate.now();

        return examDateRepository
                .findBySchoolIdAndClassNameAndExamDateAfterOrderByExamDateAsc(schoolId, className, today)
                .stream()
                .map(e -> {
                    long daysRemaining = ChronoUnit.DAYS.between(today, e.getExamDate());
                    String urgency = daysRemaining <= 7 ? "URGENT"
                            : daysRemaining <= 14 ? "UPCOMING"
                            : "NORMAL";
                    return ExamDateResponse.builder()
                            .id(e.getId())
                            .className(e.getClassName())
                            .subjectName(e.getSubjectName())
                            .examDate(e.getExamDate())
                            .daysRemaining(daysRemaining)
                            .urgency(urgency)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUGGESTION ENGINE
    // Priority order:
    //   1. Exam-based  (+50 urgent, +25 upcoming)
    //   2. Goal-based  (+20)
    //   3. Weekly rule (all WEEKLY_SUBJECTS get base score +10 as fallback)
    // ─────────────────────────────────────────────────────────────────────────

    private List<TaskSuggestion> generateSuggestions(
            List<ExamDateResponse> upcomingExams,
            Set<String> goalSubjects,
            Set<String> coveredSubjects) {

        Map<String, TaskSuggestion> suggestionMap = new LinkedHashMap<>();

        // ── Step 1: Seed weekly subjects that are NOT yet covered this week ───
        for (String subject : WEEKLY_SUBJECTS) {
            if (coveredSubjects.contains(subject)) continue;
            TaskSuggestion s = TaskSuggestion.builder()
                    .title("Study session — " + subject)
                    .subjectName(subject)
                    .reasonLabel("Weekly subject")
                    .estimatedMinutes(DEFAULT_SESSION_MINS)
                    .score(10)
                    .build();
            suggestionMap.put(subject.toLowerCase(), s);
        }

        // ── Step 2: Boost based on student goals ──────────────────────────────
        for (String goalSubject : goalSubjects) {
            String key = goalSubject.toLowerCase();
            TaskSuggestion existing = suggestionMap.get(key);
            if (existing != null) {
                existing.setScore(existing.getScore() + 20);
                existing.setReasonLabel("Matches your goal");
            } else {
                suggestionMap.put(key, TaskSuggestion.builder()
                        .title("Study session — " + goalSubject)
                        .subjectName(goalSubject)
                        .reasonLabel("Matches your goal")
                        .estimatedMinutes(DEFAULT_SESSION_MINS)
                        .score(20)
                        .build());
            }
        }

        // ── Step 3: Boost based on upcoming exams (highest priority) ──────────
        for (ExamDateResponse exam : upcomingExams) {
            String key = exam.getSubjectName().toLowerCase();
            int boost = "URGENT".equals(exam.getUrgency()) ? 50 : 25;
            String label = "Exam in " + exam.getDaysRemaining() + " day"
                    + (exam.getDaysRemaining() == 1 ? "" : "s");

            TaskSuggestion existing = suggestionMap.get(key);
            if (existing != null) {
                existing.setScore(existing.getScore() + boost);
                existing.setReasonLabel(label);
            } else {
                suggestionMap.put(key, TaskSuggestion.builder()
                        .title("Revise — " + exam.getSubjectName())
                        .subjectName(exam.getSubjectName())
                        .reasonLabel(label)
                        .estimatedMinutes(DEFAULT_SESSION_MINS)
                        .score(boost)
                        .build());
            }
        }

        // ── Step 4: Sort by score descending, return top 10 ──────────────────
        return suggestionMap.values().stream()
                .sorted(Comparator.comparingInt(TaskSuggestion::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN: Save school timings (replaces existing for that school)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<SchoolTimingResponse> saveSchoolTimings(Long schoolId,
                                                        List<SchoolTimingRequest> requests) {
        schoolTimingRepository.deleteBySchoolId(schoolId);

        List<SchoolTiming> saved = requests.stream()
                .map(r -> SchoolTiming.builder()
                        .schoolId(schoolId)
                        .className(r.getClassName())
                        .dayOfWeek(r.getDayOfWeek())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build())
                .map(schoolTimingRepository::save)
                .collect(Collectors.toList());

        return saved.stream()
                .map(t -> SchoolTimingResponse.builder()
                        .id(t.getId())
                        .className(t.getClassName())
                        .dayOfWeek(t.getDayOfWeek())
                        .startTime(t.getStartTime())
                        .endTime(t.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SchoolTimingResponse> getSchoolTimings(Long schoolId) {
        return schoolTimingRepository.findBySchoolId(schoolId).stream()
                .map(t -> SchoolTimingResponse.builder()
                        .id(t.getId())
                        .className(t.getClassName())
                        .dayOfWeek(t.getDayOfWeek())
                        .startTime(t.getStartTime())
                        .endTime(t.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN: Exam dates
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public ExamDateResponse saveExamDate(ExamDateRequest request) {
        ExamDate saved = examDateRepository.save(ExamDate.builder()
                .schoolId(request.getSchoolId())
                .className(request.getClassName())
                .subjectName(request.getSubjectName())
                .examDate(request.getExamDate())
                .build());

        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), saved.getExamDate());
        return ExamDateResponse.builder()
                .id(saved.getId())
                .className(saved.getClassName())
                .subjectName(saved.getSubjectName())
                .examDate(saved.getExamDate())
                .daysRemaining(daysRemaining)
                .urgency(daysRemaining <= 7 ? "URGENT" : daysRemaining <= 14 ? "UPCOMING" : "NORMAL")
                .build();
    }

    @Override
    public void deleteExamDate(Long examId) {
        examDateRepository.deleteById(examId);
    }

    @Override
    public List<ExamDateResponse> getExamDatesBySchool(Long schoolId) {
        LocalDate today = LocalDate.now();
        return examDateRepository.findBySchoolId(schoolId).stream()
                .map(e -> {
                    long days = ChronoUnit.DAYS.between(today, e.getExamDate());
                    return ExamDateResponse.builder()
                            .id(e.getId())
                            .className(e.getClassName())
                            .subjectName(e.getSubjectName())
                            .examDate(e.getExamDate())
                            .daysRemaining(days)
                            .urgency(days <= 7 ? "URGENT" : days <= 14 ? "UPCOMING" : "NORMAL")
                            .build();
                })
                .collect(Collectors.toList());
    }
}