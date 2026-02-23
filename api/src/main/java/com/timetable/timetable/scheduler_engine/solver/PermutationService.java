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
import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

        TimetableSolution solution = solutionMapper.fromScheduledClasses(
            allClasses,
            timeslotRepository.findAll(),
            roomRepository.findAll(),
            academicYear,
            semester
        );

        LessonAssignment targetLesson = solution.getLessonAssignments().stream()
            .filter(la -> la.getId().equals(scheduledClassId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "LessonAssignment not found for sc=" + scheduledClassId));

        int cohortYear = target.getCohort().getYear();

        log.info("Target: {} | cohortYear={} | currentSlot={} {}",
            targetLesson.getDisplayName(), cohortYear,
            target.getTimeslot().getDayOfWeek(), target.getTimeslot().getStartTime());

        List<TimeslotInfo> candidates = solution.getAvailableTimeslots().stream()
            .filter(ts -> isCorrectPeriod(ts, cohortYear))
            .filter(ts -> ts.getId() != target.getTimeslot().getId())
            .toList();

        log.info("Evaluating {} candidate slots for ScheduledClass {}", candidates.size(), scheduledClassId);

        boolean detailedLogged = false;
        List<ValidSlotResponse> valid = new ArrayList<>();
        TimeslotInfo original = targetLesson.getTimeslot();

        for (TimeslotInfo candidate : candidates) {
            targetLesson.setTimeslot(candidate);

            ScoreExplanation<TimetableSolution, HardSoftScore> explanation =
                solutionManager.explain(solution);
            HardSoftScore score = explanation.getScore();

            // Log the first failing candidate in detail so we know which constraint breaks
            if (!detailedLogged && score.hardScore() < 0) {
                detailedLogged = true;
                log.warn("=== First failing candidate: {} {} → score {} ===",
                    candidate.getDayOfWeek(), candidate.getStartTime(), score);
                explanation.getConstraintMatchTotalMap().forEach((key, cmt) -> {
                    if (cmt.getScore().hardScore() < 0) {
                        log.warn("  HARD VIOLATION  constraint='{}' score={}  matches={}",
                            key, cmt.getScore(), cmt.getConstraintMatchCount());
                    }
                });
            }

            targetLesson.setTimeslot(original);

            if (score.hardScore() == 0) {
                valid.add(new ValidSlotResponse(
                    candidate.getId(),
                    candidate.getDayOfWeek().toString(),
                    candidate.getStartTime().toString(),
                    candidate.getEndTime().toString()
                ));
            }
        }

        targetLesson.setTimeslot(original);
        log.info("ScheduledClass {} → {}/{} valid slots", scheduledClassId, valid.size(), candidates.size());
        return valid;
    }

    @Transactional
    public void applySwap(Long scheduledClassId, Long targetTimeslotId) {
        ScheduledClass sc = scheduledClassRepository.findById(scheduledClassId)
            .orElseThrow(() -> new IllegalArgumentException("ScheduledClass not found: " + scheduledClassId));

        Timeslot newTimeslot = timeslotRepository.findById(targetTimeslotId)
            .orElseThrow(() -> new IllegalArgumentException("Timeslot not found: " + targetTimeslotId));

        log.info("Swap applied: ScheduledClass {} → Timeslot {} ({} {})",
            scheduledClassId, targetTimeslotId,
            newTimeslot.getDayOfWeek(), newTimeslot.getStartTime());

        sc.setTimeslot(newTimeslot);
    }

    private boolean isCorrectPeriod(TimeslotInfo ts, int cohortYear) {
        boolean isOddYear = cohortYear % 2 != 0;
        return switch (ts.getPeriod()) {
            case MORNING   -> isOddYear;
            case AFTERNOON -> !isOddYear;
            case EVENING   -> false;
        };
    }

    public record ValidSlotResponse(
        long timeslotId,
        String dayOfWeek,
        String startTime,
        String endTime
    ) {}
}
