package com.timetable.timetable.domain.schedule.repository;

import com.timetable.timetable.domain.schedule.entity.TimeSlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

}
