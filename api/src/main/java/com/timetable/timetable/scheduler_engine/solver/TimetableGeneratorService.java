package com.timetable.timetable.scheduler_engine.solver;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.timetable.timetable.domain.schedule.entity.CohortSubject;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.Timeslot;
import com.timetable.timetable.domain.schedule.repository.CohortSubjectRepository;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;
import com.timetable.timetable.domain.schedule.repository.TimeslotRepository;
import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import com.timetable.timetable.scheduler_engine.mapper.TimetableSolutionMapper;

import org.springframework.stereotype.Service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableGeneratorService {

    private final CohortSubjectRepository cohortSubjectRepository;
    private final RoomRepository roomRepository;
    private final TimeslotRepository timeslotRepository;
    private final TimetableSolutionMapper solutionMapper;
    private final SolverManager<TimetableSolution, UUID> solverManager;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UUID generateTimetable(int academicYear, int semester, UUID jobId,
            Map<UUID, SolverJob<TimetableSolution, UUID>> jobs) {
        log.info("Starting solver for job: {}", jobId);

        List<CohortSubject> cohortSubjects = cohortSubjectRepository
                .findByAcademicYearAndSemesterAndIsActive(academicYear, semester, true);
        List<Timeslot> timeslots = timeslotRepository.findAll();
        List<Room> rooms = roomRepository.findAll();

        if (cohortSubjects.isEmpty())
            throw new IllegalStateException("No active cohort-subjects found for " + academicYear + "." + semester);
        if (timeslots.isEmpty())
            throw new IllegalStateException("No timeslots available for scheduling");
        if (rooms.isEmpty())
            throw new IllegalStateException("No rooms available for scheduling");

        TimetableSolution problem = solutionMapper.toPlanningProblem(
                cohortSubjects, timeslots, rooms, academicYear, semester);

       SolverJob<TimetableSolution, UUID> job = solverManager.solve(jobId, problem);
        jobs.put(jobId, job);
        return jobId;
    }
}
