package com.timetable.timetable.scheduler_engine.solver;

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
import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates candidate timeslot swaps for a single lesson.
 *
 * Root cause of "0/14 valid": moving to a new timeslot kept the original room,
 * which was already occupied in that slot by another lesson → Room conflict.
 *
 * Fix: for each candidate timeslot, try ALL rooms. A slot is valid if ANY room
 * produces hardScore == 0. The best room found is included in the response so
 * the frontend can show it and the swap can persist both changes.
 */
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

        List<TimeslotInfo> candidateSlots = solution.getAvailableTimeslots().stream()
            .filter(ts -> isCorrectPeriod(ts, cohortYear))
            .filter(ts -> ts.getId() != target.getTimeslot().getId())
            .toList();

        List<RoomInfo> allRooms = solution.getAvailableRooms();

        log.info("Target: {} | cohortYear={} | currentSlot={} {} | currentRoom={}",
            targetLesson.getDisplayName(), cohortYear,
            target.getTimeslot().getDayOfWeek(), target.getTimeslot().getStartTime(),
            target.getRoom().getName());

        log.info("Evaluating {} candidate slots × {} rooms for ScheduledClass {}",
            candidateSlots.size(), allRooms.size(), scheduledClassId);

        TimeslotInfo originalSlot = targetLesson.getTimeslot();
        RoomInfo originalRoom     = targetLesson.getRoom();

        List<ValidSlotResponse> valid = new ArrayList<>();

        for (TimeslotInfo candidateSlot : candidateSlots) {
            targetLesson.setTimeslot(candidateSlot);

            // Try each room — accept the first one that produces 0 hard violations
            RoomInfo validRoom = null;
            for (RoomInfo candidateRoom : allRooms) {
                targetLesson.setRoom(candidateRoom);

                ScoreExplanation<TimetableSolution, HardSoftScore> explanation =
                    solutionManager.explain(solution);

                if (explanation.getScore().hardScore() == 0) {
                    validRoom = candidateRoom;
                    break;
                }
            }

            // Always restore both planning variables before next iteration
            targetLesson.setTimeslot(originalSlot);
            targetLesson.setRoom(originalRoom);

            if (validRoom != null) {
                valid.add(new ValidSlotResponse(
                    candidateSlot.getId(),
                    candidateSlot.getDayOfWeek().toString(),
                    candidateSlot.getStartTime().toString(),
                    candidateSlot.getEndTime().toString(),
                    validRoom.getId(),
                    validRoom.getName()
                ));
            }
        }

        log.info("ScheduledClass {} → {}/{} valid slots", scheduledClassId, valid.size(), candidateSlots.size());
        return valid;
    }

    /**
     * Persists a confirmed swap — updates both timeslot AND room.
     */
    @Transactional
    public void applySwap(Long scheduledClassId, Long targetTimeslotId, Long targetRoomId) {
        ScheduledClass sc = scheduledClassRepository.findById(scheduledClassId)
            .orElseThrow(() -> new IllegalArgumentException("ScheduledClass not found: " + scheduledClassId));

        Timeslot newTimeslot = timeslotRepository.findById(targetTimeslotId)
            .orElseThrow(() -> new IllegalArgumentException("Timeslot not found: " + targetTimeslotId));

        com.timetable.timetable.domain.schedule.entity.Room newRoom =
            roomRepository.findById(targetRoomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + targetRoomId));

        log.info("Swap applied: ScheduledClass {} → Timeslot {} ({} {}) + Room {}",
            scheduledClassId, targetTimeslotId,
            newTimeslot.getDayOfWeek(), newTimeslot.getStartTime(),
            newRoom.getName());

        sc.setTimeslot(newTimeslot);
        sc.setRoom(newRoom);
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
        String endTime,
        long roomId,
        String roomName
    ) {}
}
