package com.timetable.timetable.scheduler_engine.solver;

import com.timetable.timetable.domain.schedule.entity.ScheduledClass;
import com.timetable.timetable.domain.schedule.entity.Timeslot;
import com.timetable.timetable.domain.schedule.repository.ScheduledClassRepository;
import com.timetable.timetable.domain.schedule.repository.TimeslotRepository;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;
import com.timetable.timetable.scheduler_engine.domain.LessonAssignment;
import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import com.timetable.timetable.scheduler_engine.domain.info.TimeslotInfo;
import com.timetable.timetable.scheduler_engine.mapper.TimetableSolutionMapper;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermutationService {

    private final ScheduledClassRepository scheduledClassRepository;
    private final TimeslotRepository timeslotRepository;
    private final RoomRepository roomRepository;
    private final TimetableSolutionMapper solutionMapper;
    private final SolutionManager<TimetableSolution, HardSoftScore> solutionManager;

    @Transactional(readOnly = true)
    public List<ValidSlotResponse> findValidSlots(
            Long scheduledClassId, int academicYear, int semester) {

        List<ScheduledClass> allClasses = scheduledClassRepository
                .findAllWithDetailsByPeriod(academicYear, semester);

        if (allClasses.isEmpty()) {
            throw new IllegalStateException(
                    "No persisted timetable for %d.%d".formatted(academicYear, semester));
        }

        ScheduledClass target = allClasses.stream()
                .filter(sc -> sc.getId().equals(scheduledClassId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ScheduledClass not found: " + scheduledClassId));

        Long targetCohortId = target.getCohort().getId();

        TimetableSolution solution = solutionMapper.fromScheduledClasses(
                allClasses,
                timeslotRepository.findAll(),
                roomRepository.findAll(),
                academicYear, semester);

        // ── DIAGNOSTIC: log the initial score of the rebuilt solution ──────────
        // If this is not 0hard/0soft, the solution mapper is not rebuilding correctly
        solutionManager.update(solution);
        HardSoftScore initialScore = solution.getScore();
        log.info("[DIAG] Initial score of rebuilt solution: {}", initialScore);
        if (initialScore != null && initialScore.hardScore() != 0) {
            log.warn("[DIAG] Rebuilt solution already has hard violations ({})! " +
                    "Object identity problem in fromScheduledClasses — planning variables " +
                    "are not the same instances as those in the value range.", initialScore);
        }

        // ── Key fix ────────────────────────────────────────────────────────────
        // Only consider a timeslot "occupied" if another lesson from a DIFFERENT
        // cohort is there. Lessons from the same cohort as the target don't count
        // as occupants — the slot is effectively empty for the target's cohort.
        //
        // Without this, a slot that already has a 1st-year lesson would appear as
        // a swap candidate when moving a 3rd-year lesson (they share MORNING period),
        // even though the 3rd-year cohort has no lesson there.
        // ──────────────────────────────────────────────────────────────────────
        Map<Long, List<LessonAssignment>> occupantsByTimeslotId = solution.getLessonAssignments().stream()
                .filter(la -> !la.getId().equals(scheduledClassId))
                .filter(la -> la.getTimeslot() != null)
                .filter(la -> !la.getCohortSubject().getCohort().getId().equals(targetCohortId))
                .collect(Collectors.groupingBy(la -> la.getTimeslot().getId()));

        LessonAssignment targetLesson = solution.getLessonAssignments().stream()
                .filter(la -> la.getId().equals(scheduledClassId))
                .findFirst()
                .orElseThrow();

        int cohortYear = target.getCohort().getYear();
        TimeslotInfo originalTimeslot = targetLesson.getTimeslot();

        List<TimeslotInfo> candidates = solution.getAvailableTimeslots().stream()
                .filter(ts -> isCorrectPeriod(ts, cohortYear))
                .filter(ts -> !ts.getId().equals(originalTimeslot.getId()))
                .toList();

        log.info("Evaluating {} candidate slots for ScheduledClass {}", candidates.size(), scheduledClassId);

        List<ValidSlotResponse> valid = new ArrayList<>();

        for (TimeslotInfo candidate : candidates) {
            List<LessonAssignment> occupants = occupantsByTimeslotId
                    .getOrDefault(candidate.getId(), List.of());

            // Para swap simples, só faz sentido se houver exatamente 1 ocupante
            // (trocar com múltiplos é ambíguo)
            LessonAssignment occupant = occupants.size() == 1 ? occupants.get(0) : null;

            targetLesson.setTimeslot(candidate);
            if (occupant != null)
                occupant.setTimeslot(originalTimeslot);

            try {
                solutionManager.update(solution);
                HardSoftScore score = solution.getScore();

                if (score != null && score.hardScore() == 0) {
                    if (occupant != null) {
                        ScheduledClass occupantSc = allClasses.stream()
                                .filter(sc -> sc.getId().equals(occupant.getId()))
                                .findFirst().orElse(null);

                        valid.add(ValidSlotResponse.swap(
                                candidate.getId(),
                                candidate.getDayOfWeek().toString(),
                                candidate.getStartTime().toString(),
                                candidate.getEndTime().toString(),
                                occupant.getId(),
                                occupantSc != null ? occupantSc.getSubject().getName() : "?",
                                occupantSc != null ? occupantSc.getCohort().getDisplayName() : "?"));
                    } else {
                        valid.add(ValidSlotResponse.empty(
                                candidate.getId(),
                                candidate.getDayOfWeek().toString(),
                                candidate.getStartTime().toString(),
                                candidate.getEndTime().toString()));
                    }
                }
            } finally {
                targetLesson.setTimeslot(originalTimeslot);
                if (occupant != null)
                    occupant.setTimeslot(candidate);
            }
        }

        log.info("ScheduledClass {} → {}/{} valid permutations", scheduledClassId, valid.size(), candidates.size());
        return valid;
    }

    @Transactional
    public void applySwap(Long scheduledClassId, Long targetTimeslotId, Long swapWithId) {
        ScheduledClass scX = scheduledClassRepository.findById(scheduledClassId)
                .orElseThrow(() -> new IllegalArgumentException("ScheduledClass not found: " + scheduledClassId));

        Timeslot newTimeslot = timeslotRepository.findById(targetTimeslotId)
                .orElseThrow(() -> new IllegalArgumentException("Timeslot not found: " + targetTimeslotId));

        if (swapWithId != null) {
            ScheduledClass scY = scheduledClassRepository.findById(swapWithId)
                    .orElseThrow(() -> new IllegalArgumentException("ScheduledClass not found: " + swapWithId));

            Timeslot xOriginal = scX.getTimeslot();
            scX.setTimeslot(newTimeslot);
            scY.setTimeslot(xOriginal);

            log.info("Full swap: ScheduledClass {} ↔ ScheduledClass {}", scheduledClassId, swapWithId);
        } else {
            scX.setTimeslot(newTimeslot);
            log.info("Move: ScheduledClass {} → Timeslot {}", scheduledClassId, targetTimeslotId);
        }
    }

    private boolean isCorrectPeriod(TimeslotInfo ts, int cohortYear) {
        boolean isOddYear = cohortYear % 2 != 0;
        return switch (ts.getPeriod()) {
            case MORNING -> isOddYear;
            case AFTERNOON -> !isOddYear;
            case EVENING -> false;
        };
    }

    public record ValidSlotResponse(
            long timeslotId,
            String dayOfWeek,
            String startTime,
            String endTime,
            boolean isSwap,
            Long swapWithId,
            String swapWithSubject,
            String swapWithCohort) {
        static ValidSlotResponse empty(long timeslotId, String day, String start, String end) {
            return new ValidSlotResponse(timeslotId, day, start, end, false, null, null, null);
        }

        static ValidSlotResponse swap(long timeslotId, String day, String start, String end,
                Long swapWithId, String subject, String cohort) {
            return new ValidSlotResponse(timeslotId, day, start, end, true, swapWithId, subject, cohort);
        }
    }
}
