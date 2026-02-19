package com.timetable.timetable.scheduler_engine.preparation;

import com.timetable.timetable.domain.schedule.dto.PhantomTeacherPolicy;
import com.timetable.timetable.domain.schedule.dto.TeacherAssignmentResult;
import com.timetable.timetable.domain.schedule.entity.*;
import com.timetable.timetable.domain.user.dto.CreateUser;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentService {

    private final UserService userService;

    /**
     * Distributes teacher for the lessons
     * limit to 8h/week
     */
    public TeacherAssignmentResult assignTeacher(
            Subject subject,
            List<CohortSubject> existingInSemester,
            PhantomTeacherPolicy policy) {

        Set<ApplicationUser> eligible = subject.getEligibleTeachers();

        if (eligible == null || eligible.isEmpty()) {
            return handleNoTeachers(subject, policy, existingInSemester,
                    AcademicPolicy.calculateWeeklyHours(subject.getCredits()));
        }

        Map<Long, Integer> workload = computeGlobalWorkload(eligible, existingInSemester);

        int hoursNeeded = AcademicPolicy.calculateWeeklyHours(subject.getCredits());

        // Procurar professor com menor carga que não vai ultrapassar limite
        ApplicationUser best = eligible.stream()
                .filter(t -> {
                    int current = workload.getOrDefault(t.getId(), 0);
                    return current + hoursNeeded <= AcademicPolicy.WEEKLY_TEACHING_HOURS_LIMIT;
                })
                .min(Comparator.comparingInt(t -> workload.getOrDefault(t.getId(), 0)))
                .orElse(null);

        if (best != null) {
            int newLoad = workload.getOrDefault(best.getId(), 0) + hoursNeeded;
            log.info("Assigned {} to {} ({}h/week → {}h/week)",
                    best.getUsername(), subject.getName(),
                    workload.getOrDefault(best.getId(), 0), newLoad);

            return new TeacherAssignmentResult(best, false, null);
        }

        // Todos os professores vão ultrapassar 8h
        log.warn("All teachers would exceed {}h/week for subject: {}",
                AcademicPolicy.WEEKLY_TEACHING_HOURS_LIMIT, subject.getName());

        return handleNoTeachers(subject, policy, existingInSemester, hoursNeeded);
    }

    private Map<Long, Integer> computeGlobalWorkload(
            Set<ApplicationUser> eligibleTeachers,
            List<CohortSubject> allExistingInSemester) {

        Map<Long, Integer> workload = new HashMap<>();

        Set<Long> eligibleTeacherIds = eligibleTeachers.stream()
                .map(ApplicationUser::getId)
                .collect(Collectors.toSet());

        for (CohortSubject cs : allExistingInSemester) {
            ApplicationUser teacher = cs.getAssignedTeacher();

            if (teacher != null && eligibleTeacherIds.contains(teacher.getId())) {
                workload.merge(teacher.getId(), cs.getWeeklyHours(), Integer::sum);

                log.trace("Teacher {} has {} hours from {}",
                        teacher.getUsername(), cs.getWeeklyHours(), cs.getDisplayName());
            }
        }

        return workload;
    }

    private TeacherAssignmentResult handleNoTeachers(
            Subject subject,
            PhantomTeacherPolicy policy,
            List<CohortSubject> existingInSemester,
            int hoursNeeded) {

        String baseUsername = "PHANTOM_" + subject.getId();
        int index = 1;

        while (true) {
            if (index > 50) {
                throw new IllegalStateException(
                        "Subject '" + subject.getName() + "' requires " + hoursNeeded +
                                "h/week which exceeds the " + AcademicPolicy.WEEKLY_TEACHING_HOURS_LIMIT +
                                "h/week limit. Cannot assign any teacher.");
            }
            final String username = index == 1 ? baseUsername : baseUsername + "_" + index;
            ApplicationUser candidate = userService.findByUsername(username)
                    .orElseGet(() -> createPhantom(username));

            // Check load across ALL assignments, not just eligible teachers
            int currentLoad = existingInSemester.stream()
                    .filter(cs -> cs.getAssignedTeacher() != null
                            && cs.getAssignedTeacher().getUsername().equals(username))
                    .mapToInt(CohortSubject::getWeeklyHours)
                    .sum();

            if (currentLoad + hoursNeeded <= AcademicPolicy.WEEKLY_TEACHING_HOURS_LIMIT) {
                log.warn("Using phantom teacher {} for subject {} ({}h + {}h)",
                        username, subject.getName(), currentLoad, hoursNeeded);
                return new TeacherAssignmentResult(candidate, true,
                        "Phantom teacher used for: " + subject.getName());
            }
            index++;
        }
    }

    private ApplicationUser createPhantom(String username) {
        CreateUser request = new CreateUser(
                username,
                generatePhantomEmail(),
                "phantom123",
                List.of("user", "teacher"));
        log.warn("Created phantom teacher {}", username);
        return userService.createUser(request);
    }

    private String generatePhantomEmail() {
        return "phantom+" + UUID.randomUUID() + "@placeholder.temp";
    }
}
