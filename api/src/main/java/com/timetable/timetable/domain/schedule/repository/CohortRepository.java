package com.timetable.timetable.domain.schedule.repository;

import com.timetable.timetable.domain.schedule.entity.Cohort;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CohortRepository extends JpaRepository<Cohort, Long> {
    boolean existsByYearAndSectionAndAcademicYearAndCourseId(
        int year, 
        String section, 
        int academicYear,
        Long courseId
    );

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Cohort c WHERE c.year = :year AND c.section = :section " +
           "AND c.academicYear = :academicYear AND c.course.id = :courseId " +
           "AND c.id <> :excludeId")
    boolean existsAnotherWithSameAttributes(
        @Param("year") int year,
        @Param("section") String section,
        @Param("academicYear") int academicYear,
        @Param("courseId") Long courseId,
        @Param("excludeId") Long excludeId
    );
}
