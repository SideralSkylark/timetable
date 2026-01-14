package com.timetable.timetable.domain.schedule.repository;

import com.timetable.timetable.domain.schedule.entity.Cohort;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CohortRepository extends JpaRepository<Cohort, Long> {
    
    boolean existsByYearAndSectionAndSemesterAndAcademicYearAndCourseId(
        int year, 
        String section, 
        int semester,
        int academicYear,
        Long courseId
    );

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Cohort c WHERE c.year = :year AND c.section = :section " +
           "AND c.semester = :semester AND c.academicYear = :academicYear " +
           "AND c.course.id = :courseId AND c.id <> :excludeId")
    boolean existsAnotherWithSameAttributes(
        @Param("year") int year,
        @Param("section") String section,
        @Param("semester") int semester,
        @Param("academicYear") int academicYear,
        @Param("courseId") Long courseId,
        @Param("excludeId") Long excludeId
    );
    
    List<Cohort> findByCourseId(Long courseId);
    
    List<Cohort> findByYearAndSemester(int year, int semester);
    
    List<Cohort> findByAcademicYear(int academicYear);
    
    List<Cohort> findByYear(int year);
    
    List<Cohort> findBySemester(int semester);
    
    @Query("SELECT c FROM Cohort c WHERE " +
           "(:year IS NULL OR c.year = :year) AND " +
           "(:semester IS NULL OR c.semester = :semester) AND " +
           "(:academicYear IS NULL OR c.academicYear = :academicYear) AND " +
           "(:courseId IS NULL OR c.course.id = :courseId)")
    List<Cohort> findByCriteria(
        @Param("year") Integer year,
        @Param("semester") Integer semester,
        @Param("academicYear") Integer academicYear,
        @Param("courseId") Long courseId
    );
}
