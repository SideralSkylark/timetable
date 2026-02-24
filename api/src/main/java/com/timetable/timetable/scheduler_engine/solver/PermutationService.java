package com.timetable.timetable.scheduler_engine.solver;

import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.ScheduledClass;
import com.timetable.timetable.domain.schedule.entity.Timeslot;
import com.timetable.timetable.domain.schedule.repository.ScheduledClassRepository;
import com.timetable.timetable.domain.schedule.repository.TimeslotRepository;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;
import com.timetable.timetable.scheduler_engine.domain.LessonAssignment;
import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import com.timetable.timetable.scheduler_engine.domain.info.RoomInfo;
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

        RoomInfo originalRoom = targetLesson.getRoom();

        for (TimeslotInfo candidate : candidates) {
            List<LessonAssignment> occupants = occupantsByTimeslotId
                    .getOrDefault(candidate.getId(), List.of());

            LessonAssignment occupant = occupants.size() == 1 ? occupants.get(0) : null;

            // Testar cada sala disponível
            for (RoomInfo candidateRoom : solution.getAvailableRooms()) {

                // Verificações rápidas antes de chamar o ScoreManager (performance)
                if (!candidateRoom.hasSufficientCapacity(targetLesson.getStudentCount()))
                    continue;
                if (!candidateRoom.isAvailableForCourse(targetLesson.getCourseId(), candidate.getPeriod()))
                    continue;

                targetLesson.setTimeslot(candidate);
                targetLesson.setRoom(candidateRoom);
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
                                    occupantSc != null ? occupantSc.getCohort().getDisplayName() : "?",
                                    candidateRoom.getName(),
                                    candidateRoom.getId()));
                        } else {
                            valid.add(ValidSlotResponse.empty(
                                    candidate.getId(),
                                    candidate.getDayOfWeek().toString(),
                                    candidate.getStartTime().toString(),
                                    candidate.getEndTime().toString(),
                                    candidateRoom.getName(),
                                    candidateRoom.getId()));
                        }
                        break; // Primeira sala válida chega — não precisas de mais para o mesmo slot
                    }
                } finally {
                    targetLesson.setTimeslot(originalTimeslot);
                    targetLesson.setRoom(originalRoom);
                    if (occupant != null)
                        occupant.setTimeslot(candidate);
                }
            }
        }

        log.info("ScheduledClass {} → {}/{} valid permutations", scheduledClassId, valid.size(), candidates.size());
        return valid;
    }

    @Transactional
    public void applySwap(Long scheduledClassId, Long targetTimeslotId, Long targetRoomId, Long swapWithId) {
        ScheduledClass scX = scheduledClassRepository.findById(scheduledClassId)
                .orElseThrow(() -> new IllegalArgumentException("ScheduledClass not found: " + scheduledClassId));

        Timeslot newTimeslot = timeslotRepository.findById(targetTimeslotId)
                .orElseThrow(() -> new IllegalArgumentException("Timeslot not found: " + targetTimeslotId));

        Room newRoom = roomRepository.findById(targetRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + targetRoomId));

        if (swapWithId != null) {
            ScheduledClass scY = scheduledClassRepository.findById(swapWithId)
                    .orElseThrow(() -> new IllegalArgumentException("ScheduledClass not found: " + swapWithId));

            Timeslot xOriginal = scX.getTimeslot();
            scX.setTimeslot(newTimeslot);
            scX.setRoom(newRoom); // ← sala também muda
            scY.setTimeslot(xOriginal);
            // scY mantém a sua sala original

            log.info("Full swap: ScheduledClass {} ↔ ScheduledClass {}", scheduledClassId, swapWithId);
        } else {
            scX.setTimeslot(newTimeslot);
            scX.setRoom(newRoom); // ← sala também muda
            log.info("Move: ScheduledClass {} → Timeslot {} Room {}", scheduledClassId, targetTimeslotId, targetRoomId);
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

    public record CohortSwapCandidate(
            Long scheduledClassId,
            String subjectName,
            String dayOfWeek,
            String startTime,
            String roomName) {
    }

    @Transactional(readOnly = true)
    public List<CohortSwapCandidate> findCohortSwapCandidates(
            Long scheduledClassId, int academicYear, int semester) {

        List<ScheduledClass> allClasses = scheduledClassRepository
                .findAllWithDetailsByPeriod(academicYear, semester);

        ScheduledClass target = allClasses.stream()
                .filter(sc -> sc.getId().equals(scheduledClassId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ScheduledClass not found: " + scheduledClassId));

        Long targetCohortId = target.getCohort().getId();
        Long targetSubjectId = target.getSubject().getId();

        TimetableSolution solution = solutionMapper.fromScheduledClasses(
                allClasses, timeslotRepository.findAll(), roomRepository.findAll(),
                academicYear, semester);

        LessonAssignment targetLesson = solution.getLessonAssignments().stream()
                .filter(la -> la.getId().equals(scheduledClassId))
                .findFirst().orElseThrow();

        // Outras aulas da mesma cohort, disciplina diferente
        List<LessonAssignment> sameCohortOthers = solution.getLessonAssignments().stream()
                .filter(la -> !la.getId().equals(scheduledClassId))
                .filter(la -> la.getCohortSubject().getCohort().getId().equals(targetCohortId))
                .filter(la -> !la.getCohortSubject().getSubject().getId().equals(targetSubjectId))
                .toList();

        TimeslotInfo originalTimeslotA = targetLesson.getTimeslot();
        RoomInfo originalRoomA = targetLesson.getRoom();

        List<CohortSwapCandidate> valid = new ArrayList<>();

        for (LessonAssignment candidate : sameCohortOthers) {
            TimeslotInfo originalTimeslotB = candidate.getTimeslot();
            RoomInfo originalRoomB = candidate.getRoom();

            // Trocar timeslots (salas ficam iguais — cada aula leva a sua sala)
            targetLesson.setTimeslot(originalTimeslotB);
            candidate.setTimeslot(originalTimeslotA);

            try {
                solutionManager.update(solution);
                HardSoftScore score = solution.getScore();

                if (score != null && score.hardScore() == 0) {
                    ScheduledClass candidateSc = allClasses.stream()
                            .filter(sc -> sc.getId().equals(candidate.getId()))
                            .findFirst().orElseThrow();

                    valid.add(new CohortSwapCandidate(
                            candidate.getId(),
                            candidateSc.getSubject().getName(),
                            originalTimeslotB.getDayOfWeek().toString(),
                            originalTimeslotB.getStartTime().toString(),
                            originalRoomB.getName()));
                }
            } finally {
                targetLesson.setTimeslot(originalTimeslotA);
                candidate.setTimeslot(originalTimeslotB);
            }
        }

        log.info("ScheduledClass {} → {}/{} valid cohort swaps",
                scheduledClassId, valid.size(), sameCohortOthers.size());
        return valid;
    }

    @Transactional
    public void applyCohortSwap(Long scheduledClassIdA, Long scheduledClassIdB) {
        ScheduledClass scA = scheduledClassRepository.findById(scheduledClassIdA)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + scheduledClassIdA));
        ScheduledClass scB = scheduledClassRepository.findById(scheduledClassIdB)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + scheduledClassIdB));

        Timeslot timeslotA = scA.getTimeslot();
        scA.setTimeslot(scB.getTimeslot());
        scB.setTimeslot(timeslotA);
        // Salas não mudam — cada aula mantém a sua sala original

        log.info("Cohort swap: ScheduledClass {} ↔ ScheduledClass {}", scheduledClassIdA, scheduledClassIdB);
    }

    public record ValidSlotResponse(
            long timeslotId,
            String dayOfWeek,
            String startTime,
            String endTime,
            boolean isSwap,
            Long swapWithId,
            String swapWithSubject,
            String swapWithCohort,
            String roomName, // ← NOVO: sala onde a aula ficará
            Long roomId) { // ← NOVO: para persistir a mudança de sala

        static ValidSlotResponse empty(long timeslotId, String day, String start, String end,
                String roomName, Long roomId) {
            return new ValidSlotResponse(timeslotId, day, start, end, false, null, null, null, roomName, roomId);
        }

        static ValidSlotResponse swap(long timeslotId, String day, String start, String end,
                Long swapWithId, String subject, String cohort, String roomName, Long roomId) {
            return new ValidSlotResponse(timeslotId, day, start, end, true, swapWithId, subject, cohort, roomName,
                    roomId);
        }
    }
}
