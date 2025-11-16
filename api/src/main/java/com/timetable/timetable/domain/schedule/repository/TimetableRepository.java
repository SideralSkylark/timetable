package com.timetable.timetable.domain.schedule.repository;

import java.util.List;
import java.util.Optional;

import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.schedule.entity.TimetableStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByStatus(TimetableStatus status);

    Optional<Timetable> findByAcademicPeriod(String academicPeriod);

    Optional<Timetable> findTopByOrderByCreatedAtDesc();

    List<Timetable> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    boolean existsByAcademicPeriod(String academicPeriod);
Page<Timetable> findByStatus(TimetableStatus status, Pageable pageable);
}
