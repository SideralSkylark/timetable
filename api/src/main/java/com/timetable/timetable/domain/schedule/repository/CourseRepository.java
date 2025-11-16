package com.timetable.timetable.domain.schedule.repository;

import com.timetable.timetable.domain.schedule.entity.Course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByName(String name);
    Page<Course> findAll(Pageable pageable);
}
