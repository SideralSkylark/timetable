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
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
Page<TimeSlot> findByTimetable(Timetable timetable, Pageable pageable);
Page<TimeSlot> findByCohort(Cohort cohort, Pageable pageable);
Page<TimeSlot> findByTeacher(ApplicationUser teacher, Pageable pageable);

// For conflict detection
List<TimeSlot> findByTeacherAndDateAndTimeOverlap(
    ApplicationUser teacher, LocalDate date, LocalTime startTime, LocalTime endTime);
List<TimeSlot> findByRoomAndDateAndTimeOverlap(
    Room room, LocalDate date, LocalTime startTime, LocalTime endTime);
List<TimeSlot> findByCohortAndDateAndTimeOverlap(
    Cohort cohort, LocalDate date, LocalTime startTime, LocalTime endTime);
}
