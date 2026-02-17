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

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentService {

    private final UserService userService;

    /**
     * Distribui professores pelas disciplinas
     * respeitando limite de 8h/semana
     */
    public TeacherAssignmentResult assignTeacher(
            Subject subject,
            List<CohortSubject> existingInSemester,
            PhantomTeacherPolicy policy) {

        Set<ApplicationUser> eligible = subject.getEligibleTeachers();

        if (eligible == null || eligible.isEmpty()) {
            return handleNoTeachers(subject, policy);
        }

        // Calcular carga atual (horas/semana) de cada professor elegível
        Map<Long, Integer> workload = computeWorkload(eligible, existingInSemester);

        // Horas que esta disciplina vai adicionar
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

        return handleNoTeachers(subject, policy);
    }

    private Map<Long, Integer> computeWorkload(
            Set<ApplicationUser> teachers,
            List<CohortSubject> existing) {

        Map<Long, Integer> workload = new HashMap<>();

        for (CohortSubject cs : existing) {
            ApplicationUser t = cs.getAssignedTeacher();
            if (t != null && teachers.contains(t)) {
                workload.merge(t.getId(), cs.getWeeklyHours(), Integer::sum);
            }
        }

        return workload;
    }

    private TeacherAssignmentResult handleNoTeachers(
            Subject subject,
            PhantomTeacherPolicy policy) {

        if (policy == PhantomTeacherPolicy.NEVER) {
            throw new IllegalStateException(
                    "No available teacher for: " + subject.getName());
        }

        CreateUser request = new CreateUser(
                "PHANTOM_" + subject.getName().replaceAll("\\s+", "_"),
                generatePhantomEmail(),
                "phantom123",
                List.of("user", "teacher"));

        ApplicationUser phantom = userService.createUser(request);

        log.warn("Created phantom teacher {} for subject {}",
                phantom.getUsername(), subject.getName());

        return new TeacherAssignmentResult(
                phantom, true,
                "Phantom teacher created for: " + subject.getName());
    }

    private String generatePhantomEmail() {
        return "phantom+" + UUID.randomUUID() + "@placeholder.temp";
    }
}
