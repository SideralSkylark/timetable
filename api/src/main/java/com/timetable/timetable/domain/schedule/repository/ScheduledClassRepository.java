package com.timetable.timetable.domain.schedule.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.timetable.timetable.domain.schedule.entity.Cohort;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.ScheduledClass;
import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.user.entity.ApplicationUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledClassRepository extends JpaRepository<ScheduledClass, Long> {
Page<ScheduledClass> findByTimetable(Timetable timetable, Pageable pageable);
Page<ScheduledClass> findByCohort(Cohort cohort, Pageable pageable);
Page<ScheduledClass> findByTeacher(ApplicationUser teacher, Pageable pageable);

// For conflict detection
@Query("SELECT ts FROM ScheduledClass ts WHERE ts.teacher = :teacher AND ts.date = :date " +
           "AND ts.startTime < :endTime AND ts.endTime > :startTime")
    List<ScheduledClass> findByTeacherAndDateAndTimeOverlap(
        @Param("teacher") ApplicationUser teacher, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("endTime") LocalTime endTime);
    
    @Query("SELECT ts FROM ScheduledClass ts WHERE ts.room = :room AND ts.date = :date " +
           "AND ts.startTime < :endTime AND ts.endTime > :startTime")
    List<ScheduledClass> findByRoomAndDateAndTimeOverlap(
        @Param("room") Room room, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("endTime") LocalTime endTime);
    
    @Query("SELECT ts FROM ScheduledClass ts WHERE ts.cohort = :cohort AND ts.date = :date " +
           "AND ts.startTime < :endTime AND ts.endTime > :startTime")
    List<ScheduledClass> findByCohortAndDateAndTimeOverlap(
        @Param("cohort") Cohort cohort, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("endTime") LocalTime endTime);
}
