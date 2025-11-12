package com.timetable.timetable.domain.schedule.entity;

import java.util.HashSet;
import java.util.Set;

import com.timetable.timetable.domain.user.entity.ApplicationUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cohorts")
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name; // ex: "Turma A-2024"
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    // Estudantes nesta turma
    @ManyToMany
    @JoinTable(
        name = "cohort_students",
        joinColumns = @JoinColumn(name = "cohort_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<ApplicationUser> students = new HashSet<>();
}
