package com.timetable.timetable.domain.schedule.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.timetable.timetable.domain.schedule.entity.Cohort;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.TimeSlot;
import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.user.entity.ApplicationUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
Page<TimeSlot> findByTimetable(Timetable timetable, Pageable pageable);
Page<TimeSlot> findByCohort(Cohort cohort, Pageable pageable);
Page<TimeSlot> findByTeacher(ApplicationUser teacher, Pageable pageable);

// For conflict detection
@Query("SELECT ts FROM TimeSlot ts WHERE ts.teacher = :teacher AND ts.date = :date " +
           "AND ts.startTime < :endTime AND ts.endTime > :startTime")
    List<TimeSlot> findByTeacherAndDateAndTimeOverlap(
        @Param("teacher") ApplicationUser teacher, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("endTime") LocalTime endTime);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.room = :room AND ts.date = :date " +
           "AND ts.startTime < :endTime AND ts.endTime > :startTime")
    List<TimeSlot> findByRoomAndDateAndTimeOverlap(
        @Param("room") Room room, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("endTime") LocalTime endTime);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.cohort = :cohort AND ts.date = :date " +
           "AND ts.startTime < :endTime AND ts.endTime > :startTime")
    List<TimeSlot> findByCohortAndDateAndTimeOverlap(
        @Param("cohort") Cohort cohort, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("endTime") LocalTime endTime);
}
