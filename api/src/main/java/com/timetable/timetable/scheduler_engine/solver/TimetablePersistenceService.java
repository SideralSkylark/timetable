package com.timetable.timetable.scheduler_engine.solver;

import com.timetable.timetable.domain.schedule.entity.*;
import com.timetable.timetable.domain.schedule.repository.TimetableRepository;
import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import com.timetable.timetable.scheduler_engine.mapper.PersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persists a solved TimetableSolution into Timetable + ScheduledClass.
 * Delegates entity resolution to the existing PersistenceMapper.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimetablePersistenceService {

    private final TimetableRepository timetableRepository;
    private final PersistenceMapper persistenceMapper;

    @Transactional
    public Timetable saveSolution(TimetableSolution solution) {
        int year = solution.getAcademicYear();
        int semester = solution.getSemester();

        log.info("Persisting timetable {}.{} — {} lessons ({} unassigned)",
                year, semester, solution.getTotalLessons(), solution.getUnassignedLessons());

        // Replace any previous solution for this period (cascade deletes ScheduledClass
        // rows)
        timetableRepository.deleteByAcademicYearAndSemester(year, semester);

        Timetable timetable = Timetable.builder()
                .academicYear(year)
                .semester(semester)
                .status(TimetableStatus.DRAFT)
                .build();

        // PersistenceMapper already resolves CohortSubject / Timeslot / Room by ID
        List<ScheduledClass> classes = persistenceMapper.convertToScheduledClasses(solution, timetable);
        timetable.setScheduledClasses(classes);

        Timetable saved = timetableRepository.save(timetable);

        log.info("Saved timetable id={} for {}.{} with {} scheduled classes",
                saved.getId(), year, semester, classes.size());

        return saved;
    }
}
