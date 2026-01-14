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
     * A turma que vai ter esta disciplina
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;
    
    /**
     * A disciplina que será lecionada
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    
    /**
     * O professor atribuído para lecionar esta disciplina a esta turma específica.
     * Escolhido da lista subject.eligibleTeachers
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_teacher_id", nullable = false)
    private ApplicationUser assignedTeacher;
    
    /**
     * Ano académico desta atribuição (ex: 2026)
     * Deve coincidir com cohort.academicYear
     */
    @Column(nullable = false)
    private int academicYear;
    
    /**
     * Semestre desta atribuição (1 ou 2)
     * Deve coincidir com cohort.semester e subject.targetSemester
     */
    @Column(nullable = false)
    private int semester;
    
    /**
     * Permite desativar temporariamente esta atribuição
     * (ex: professor de baixa médica, disciplina suspensa)
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
    
    /**
     * Número de horas semanais que esta disciplina terá para esta turma
     * (pode ser diferente de outras turmas da mesma disciplina)
     */
    @Column(nullable = false)
    @Builder.Default
    private int weeklyHours = 4;
    
    /**
     * Número de aulas por semana
     * (usado pelo solver para saber quantas ScheduledClass criar)
     */
    @Column(nullable = false)
    @Builder.Default
    private int lessonsPerWeek = 2;
    
    // ====== MÉTODOS HELPER ======
    
    /**
     * Retorna a duração de cada aula em minutos
     */
    public int getMinutesPerLesson() {
        int totalWeeklyMinutes = weeklyHours * 60;
        return totalWeeklyMinutes / lessonsPerWeek;
    }
    
    /**
     * Retorna o número total de aulas no semestre
     * (assumindo 16 semanas por semestre)
     */
    public int getTotalLessonsInSemester() {
        return lessonsPerWeek * 16; // 16 semanas padrão
    }
    
    /**
     * Identificador único para display
     */
    public String getDisplayName() {
        return cohort.getDisplayName() + " - " + subject.getName() + 
               " (" + assignedTeacher.getUsername() + ")";
    }
    
    /**
     * Valida se o professor atribuído é elegível para lecionar esta disciplina
     */
    public boolean isTeacherEligible() {
        return subject.getEligibleTeachers().contains(assignedTeacher);
    }
    
    /**
     * Valida consistência de dados
     */
    public boolean isValid() {
        return cohort.getAcademicYear() == academicYear &&
               cohort.getSemester() == semester &&
               subject.getTargetSemester() == semester &&
               isTeacherEligible();
    }
}
