package com.timetable.timetable.domain.schedule.repository;

import java.util.List;
import java.util.Optional;

import com.timetable.timetable.domain.schedule.entity.*;
import com.timetable.timetable.domain.user.entity.ApplicationUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledClassRepository extends JpaRepository<ScheduledClass, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "cohortSubject",
            "cohortSubject.cohort",
            "cohortSubject.subject",
            "cohortSubject.assignedTeacher",
            "room",
            "timeslot",
            "timetable"
    })
    Page<ScheduledClass> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
            "cohortSubject",
            "cohortSubject.cohort",
            "cohortSubject.subject",
            "cohortSubject.assignedTeacher",
            "room",
            "timeslot"
    })
    Page<ScheduledClass> findByTimetable(Timetable timetable, Pageable pageable);

    @Query("""
        SELECT sc FROM ScheduledClass sc
        JOIN FETCH sc.cohortSubject cs
        JOIN FETCH cs.cohort
        JOIN FETCH cs.subject
        JOIN FETCH cs.assignedTeacher
        JOIN FETCH sc.room
        JOIN FETCH sc.timeslot
        LEFT JOIN FETCH sc.timetable
        WHERE sc.id = :id
    """)
    Optional<ScheduledClass> findByIdWithDetails(@Param("id") Long id);

    Page<ScheduledClass> findByCohortSubjectId(Long cohortSubjectId, Pageable pageable);

    @Query("""
        SELECT sc FROM ScheduledClass sc
        WHERE sc.cohortSubject.cohort.id = :cohortId
    """)
    Page<ScheduledClass> findByCohortId(@Param("cohortId") Long cohortId, Pageable pageable);

    @Query("""
        SELECT sc FROM ScheduledClass sc
        WHERE sc.cohortSubject.assignedTeacher.id = :teacherId
    """)
    Page<ScheduledClass> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    @Query("""
        SELECT sc FROM ScheduledClass sc
        WHERE (
            sc.cohortSubject.assignedTeacher = :teacher
            OR sc.cohortSubject.cohort = :cohort
            OR sc.room = :room
        )
        AND sc.timeslot = :timeslot
        AND (:timetable IS NULL OR sc.timetable = :timetable)
    """)
    List<ScheduledClass> findConflicts(
            @Param("teacher") ApplicationUser teacher,
            @Param("cohort") Cohort cohort,
            @Param("room") Room room,
            @Param("timeslot") Timeslot timeslot,
            @Param("timetable") Timetable timetable
    );

    int countByCohortSubjectId(Long cohortSubjectId);

    /**
     * Deletes all scheduled classes for a specific timetable.
     * Useful when regenerating a timetable.
     */
    void deleteByTimetableId(Long timetableId);
}
