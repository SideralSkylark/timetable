package com.timetable.timetable.domain.schedule.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.timetable.timetable.domain.schedule.entity.Cohort;
import com.timetable.timetable.domain.schedule.entity.CohortSubject;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.ScheduledClass;
import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.user.entity.ApplicationUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledClassRepository extends JpaRepository<ScheduledClass, Long> {
    
    @EntityGraph(attributePaths = {"cohortSubject", "cohortSubject.cohort", 
        "cohortSubject.subject", "cohortSubject.assignedTeacher", "room", "timetable"})
    Page<ScheduledClass> findAllWithDetails(Pageable pageable);
    
    @EntityGraph(attributePaths = {"cohortSubject", "cohortSubject.cohort", 
        "cohortSubject.subject", "cohortSubject.assignedTeacher", "room"})
    Page<ScheduledClass> findByTimetableWithDetails(Timetable timetable, Pageable pageable);
    
    @EntityGraph(attributePaths = {"cohortSubject", "cohortSubject.subject", 
        "cohortSubject.assignedTeacher", "room", "timetable"})
    Page<ScheduledClass> findByCohortIdWithDetails(Long cohortId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"cohortSubject", "cohortSubject.cohort", 
        "cohortSubject.subject", "room", "timetable"})
    Page<ScheduledClass> findByTeacherIdWithDetails(Long teacherId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"cohortSubject.cohort", "cohortSubject.subject", 
        "cohortSubject.assignedTeacher", "room", "timetable"})
    Page<ScheduledClass> findByCohortSubjectIdWithDetails(Long cohortSubjectId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"cohortSubject", "cohortSubject.cohort", 
        "cohortSubject.subject", "cohortSubject.assignedTeacher", "room", "timetable"})
    Page<ScheduledClass> findByDateBetweenWithDetails(
        LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    @EntityGraph(attributePaths = {"cohortSubject", "cohortSubject.cohort", 
        "cohortSubject.subject", "cohortSubject.assignedTeacher", "room", "timetable"})
    Optional<ScheduledClass> findWithDetailsById(Long id);
    
    // Métodos de consulta para conflitos (usando CohortSubject)
    @Query("SELECT sc FROM ScheduledClass sc " +
           "WHERE sc.cohortSubject.assignedTeacher = :teacher " +
           "AND sc.date = :date " +
           "AND sc.startTime < :endTime AND sc.endTime > :startTime")
    List<ScheduledClass> findByTeacherAndDateAndTimeOverlap(
        @Param("teacher") ApplicationUser teacher, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("endTime") LocalTime endTime);
    
    @Query("SELECT sc FROM ScheduledClass sc " +
           "WHERE sc.room = :room " +
           "AND sc.date = :date " +
           "AND sc.startTime < :endTime AND sc.endTime > :startTime")
    List<ScheduledClass> findByRoomAndDateAndTimeOverlap(
        @Param("room") Room room, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("endTime") LocalTime endTime);
    
    @Query("SELECT sc FROM ScheduledClass sc " +
           "WHERE sc.cohortSubject.cohort = :cohort " +
           "AND sc.date = :date " +
           "AND sc.startTime < :endTime AND sc.endTime > :startTime")
    List<ScheduledClass> findByCohortAndDateAndTimeOverlap(
        @Param("cohort") Cohort cohort, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("endTime") LocalTime endTime);
    
    // Métodos de consulta adicionais
    List<ScheduledClass> findByCohortSubject(CohortSubject cohortSubject);
    
    @Query("SELECT sc FROM ScheduledClass sc WHERE sc.cohortSubject.cohort.id = :cohortId " +
           "AND sc.date BETWEEN :startDate AND :endDate " +
           "ORDER BY sc.date, sc.startTime")
    List<ScheduledClass> findByCohortIdAndDateBetween(
        @Param("cohortId") Long cohortId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT sc FROM ScheduledClass sc WHERE sc.cohortSubject.id = :cohortSubjectId " +
           "ORDER BY sc.date, sc.startTime")
    List<ScheduledClass> findByCohortSubjectId(@Param("cohortSubjectId") Long cohortSubjectId);
    
    int countByCohortSubjectId(Long cohortSubjectId);
    
    @Query("SELECT sc FROM ScheduledClass sc WHERE " +
           "(:cohortId IS NULL OR sc.cohortSubject.cohort.id = :cohortId) AND " +
           "(:teacherId IS NULL OR sc.cohortSubject.assignedTeacher.id = :teacherId) AND " +
           "(:subjectId IS NULL OR sc.cohortSubject.subject.id = :subjectId) AND " +
           "(:roomId IS NULL OR sc.room.id = :roomId) AND " +
           "(:dateFrom IS NULL OR sc.date >= :dateFrom) AND " +
           "(:dateTo IS NULL OR sc.date <= :dateTo)")
    Page<ScheduledClass> search(
        @Param("cohortId") Long cohortId,
        @Param("teacherId") Long teacherId,
        @Param("subjectId") Long subjectId,
        @Param("roomId") Long roomId,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        Pageable pageable);
}
