package com.timetable.timetable.domain.schedule.repository;

import com.timetable.timetable.domain.schedule.entity.Cohort;
import com.timetable.timetable.domain.schedule.entity.CohortSubject;
import com.timetable.timetable.domain.schedule.entity.Subject;
import com.timetable.timetable.domain.user.entity.ApplicationUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CohortSubjectRepository extends JpaRepository<CohortSubject, Long> {
    
    boolean existsByCohortAndSubjectAndAcademicYearAndSemester(
        Cohort cohort, Subject subject, int academicYear, int semester);
    
    @Query("SELECT cs FROM CohortSubject cs " +
           "JOIN FETCH cs.cohort " +
           "JOIN FETCH cs.subject " +
           "JOIN FETCH cs.assignedTeacher " +
           "WHERE cs.id = :id")
    Optional<CohortSubject> findByIdWithDetails(@Param("id") Long id);
    
    @Override
    @EntityGraph(attributePaths = {"cohort", "subject", "assignedTeacher"})
    Page<CohortSubject> findAll(Pageable pageable);
    
    @EntityGraph(attributePaths = {"subject", "assignedTeacher"})
    Page<CohortSubject> findByCohort(Cohort cohort, Pageable pageable);
    
    @EntityGraph(attributePaths = {"cohort", "assignedTeacher"})
    Page<CohortSubject> findBySubject(Subject subject, Pageable pageable);
    
    @EntityGraph(attributePaths = {"cohort", "subject"})
    Page<CohortSubject> findByAssignedTeacher(ApplicationUser teacher, Pageable pageable);
    
    @Query("SELECT cs FROM CohortSubject cs " +
           "JOIN FETCH cs.cohort " +
           "JOIN FETCH cs.subject " +
           "JOIN FETCH cs.assignedTeacher " +
           "WHERE cs.academicYear = :academicYear AND cs.semester = :semester")
    List<CohortSubject> findByAcademicYearAndSemester(@Param("academicYear") int academicYear, 
                                                     @Param("semester") int semester);
    
    List<CohortSubject> findByCohortAndIsActive(Cohort cohort, boolean isActive);
    
    @EntityGraph(attributePaths = {"cohort", "subject"})
    List<CohortSubject> findByAssignedTeacher(ApplicationUser teacher);
    
    @Query("SELECT cs FROM CohortSubject cs " +
           "JOIN FETCH cs.subject " +
           "JOIN FETCH cs.assignedTeacher " +
           "WHERE cs.cohort = :cohort AND cs.isActive = true")
    List<CohortSubject> findActiveByCohort(@Param("cohort") Cohort cohort);
    
    @Query("SELECT SUM(cs.weeklyHours) FROM CohortSubject cs WHERE cs.assignedTeacher = :teacher AND cs.isActive = true")
    Integer sumWeeklyHoursByTeacher(@Param("teacher") ApplicationUser teacher);
    
    @Query("SELECT SUM(cs.weeklyHours) FROM CohortSubject cs WHERE cs.cohort = :cohort AND cs.isActive = true")
    Integer sumWeeklyHoursByCohort(@Param("cohort") Cohort cohort);
    
    @Query("SELECT COUNT(sc) FROM ScheduledClass sc WHERE sc.cohortSubject.id = :cohortSubjectId")
    int countScheduledClassesByCohortSubjectId(@Param("cohortSubjectId") Long cohortSubjectId);
    
    // Busca avançada
    @Query("SELECT cs FROM CohortSubject cs " +
           "LEFT JOIN cs.cohort c " +
           "LEFT JOIN cs.subject s " +
           "LEFT JOIN cs.assignedTeacher t " +
           "WHERE (:cohortId IS NULL OR c.id = :cohortId) AND " +
           "(:subjectId IS NULL OR s.id = :subjectId) AND " +
           "(:teacherId IS NULL OR t.id = :teacherId) AND " +
           "(:academicYear IS NULL OR cs.academicYear = :academicYear) AND " +
           "(:semester IS NULL OR cs.semester = :semester) AND " +
           "(:isActive IS NULL OR cs.isActive = :isActive)")
    Page<CohortSubject> search(
        @Param("cohortId") Long cohortId,
        @Param("subjectId") Long subjectId,
        @Param("teacherId") Long teacherId,
        @Param("academicYear") Integer academicYear,
        @Param("semester") Integer semester,
        @Param("isActive") Boolean isActive,
        Pageable pageable);
}
