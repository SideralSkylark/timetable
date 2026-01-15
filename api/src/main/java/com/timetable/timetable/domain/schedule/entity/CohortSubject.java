package com.timetable.timetable.domain.schedule.entity;

import com.timetable.timetable.domain.user.entity.ApplicationUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa a atribuição de uma disciplina a uma turma específica com um professor específico.
 * Esta é a "instância concreta" de uma disciplina sendo lecionada.
 * 
 * Exemplo:
 * - Cohort: "2º ano - Eng. Informática - Turma A - 2026"
 * - Subject: "Programação II"
 * - AssignedTeacher: "Prof. João Silva"
 * 
 * Esta entidade responde à pergunta: 
 * "Que disciplinas esta turma tem e com que professores?"
 */
@Entity
@Table(name = "cohort_subjects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CohortSubject {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The cohort(class) to have this subject
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;
    
    /**
     * The subject to be lectured
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    
    /**
     * The teacher assigned to this subject in this specific class. Idealy from the eligible teachers list
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_teacher_id", nullable = false)
    private ApplicationUser assignedTeacher;
    
    /**
     * The academic year (ie: 2026) must coincide with it's associated entities
     */
    @Column(nullable = false)
    private int academicYear;
    
    /**
     * The semester (1 or 2) must coincide with its associated entities
     */
    @Column(nullable = false)
    private int semester;
    
    /**
     * Allows temporary disabling of this atribution (when a teacher is sick, or a subject is suspended)
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
    
    /**
     * Number of weekly hours this class has per week
     */
    @Column(nullable = false)
    @Builder.Default
    private int weeklyHours = 4;
    
    /**
     * Number of lessons per week (so the solver knows how many scheduled classes to create)
     */
    @Column(nullable = false)
    @Builder.Default
    private int lessonsPerWeek = 2;
    
    /**
     * Lesson duration in minutes
     * @return int
     */
    public int getMinutesPerLesson() {
        int totalWeeklyMinutes = weeklyHours * 60;
        return totalWeeklyMinutes / lessonsPerWeek;
    }
    
    /**
     * Total number of lessons per semester(assuming 16 weeks per semester)
     * @return int
     */
    public int getTotalLessonsInSemester() {
        return lessonsPerWeek * 16; // 16 semanas padrão
    }
    
    /**
     * To String like method for display purposes
     * @return String
     */
    public String getDisplayName() {
        return cohort.getDisplayName() + " - " + subject.getName() + 
               " (" + assignedTeacher.getUsername() + ")";
    }
    
    /**
     * Check if the assigned teacher is eligible to teach this lesson
     * @return boolean
     */
    public boolean isTeacherEligible() {
        return subject.getEligibleTeachers().contains(assignedTeacher);
    }
    
    /**
     * check data consistency against it's cohort
     * @return boolean
     */
    public boolean isValid() {
        return cohort.getAcademicYear() == academicYear &&
               cohort.getSemester() == semester &&
               subject.getTargetSemester() == semester &&
               isTeacherEligible();
    }
}
