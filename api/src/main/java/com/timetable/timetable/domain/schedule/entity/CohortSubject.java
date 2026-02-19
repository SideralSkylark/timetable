package com.timetable.timetable.domain.schedule.entity;

import com.timetable.timetable.domain.user.entity.ApplicationUser;
import jakarta.persistence.*;
import lombok.*;

/**
 * Representa a atribuição de uma disciplina a uma turma específica com um
 * professor específico.
 * Esta é a "instância concreta" de uma disciplina sendo lecionada.
 */
@Entity
@Table(name = "cohort_subjects", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "cohort_id", "subject_id", "academic_year", "semester" })
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CohortSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_teacher_id", nullable = false)
    private ApplicationUser assignedTeacher;

    @Column(nullable = false)
    private int academicYear;

    @Column(nullable = false)
    private int semester;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /**
     * Horas semanais calculadas a partir dos créditos da disciplina
     */
    public int getWeeklyHours() {
        return AcademicPolicy.calculateWeeklyHours(subject.getCredits());
    }

    /**
     * Blocos de aula por semana (cada bloco = 110 minutos)
     */
    public int getLessonBlocksPerWeek() {
        return AcademicPolicy.calculateLessonBlocksPerWeek(subject.getCredits());
    }

    /**
     * Slots de 50 minutos por semana
     */
    public int getSlotsPerWeek() {
        return AcademicPolicy.calculateSlotsPerWeek(subject.getCredits());
    }

    /**
     * Duração de cada bloco de aula em minutos
     */
    public int getMinutesPerLessonBlock() {
        return AcademicPolicy.LESSON_BLOCK_DURATION_MINUTES;
    }

    /**
     * Total de blocos de aula no semestre
     */
    public int getTotalLessonBlocksInSemester() {
        return getLessonBlocksPerWeek() * AcademicPolicy.WEEKS_PER_SEMESTER;
    }

    /**
     * Nome para exibição
     */
    public String getDisplayName() {
        return cohort.getDisplayName() + " - " + subject.getName() +
                " (" + assignedTeacher.getUsername() + ")";
    }

    /**
     * Verifica se o professor é elegível para lecionar esta disciplina
     */
    public boolean isTeacherEligible() {
        // Phantom teachers are always considered eligible
        if (assignedTeacher.getUsername().startsWith("PHANTOM_")) {
            return true;
        }
        return subject.getEligibleTeachers().contains(assignedTeacher);
    }

    /**
     * Valida consistência de dados
     */
    public boolean isValid() {
        // Verifica alinhamento de ano académico e semestre
        if (cohort.getAcademicYear() != academicYear || cohort.getSemester() != semester) {
            return false;
        }

        // Verifica se a disciplina é do semestre correto
        if (subject.getTargetSemester() != semester) {
            return false;
        }

        // Verifica se o professor é elegível
        if (!isTeacherEligible()) {
            return false;
        }

        // Verifica se a disciplina pertence ao curso da turma
        if (!subject.getCourse().equals(cohort.getCourse())) {
            return false;
        }

        return true;
    }

    /**
     * Validação antes de persistir
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        if (!isValid()) {
            throw new IllegalStateException(
                    "CohortSubject inválido: " + getDisplayName() +
                            " - Verifique alinhamento de ano/semestre, curso e elegibilidade do professor");
        }
    }
}
