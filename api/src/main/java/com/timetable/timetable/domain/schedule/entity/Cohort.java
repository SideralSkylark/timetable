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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cohorts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int year; // ex: 1st, 2nd, 3rd, ...

    private String section; // ex: A, B, C, ...

    private int academicYear; // ex: 2026
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String courseNameSnapshot;
    
    @ManyToMany
    @JoinTable(
        name = "cohort_students",
        joinColumns = @JoinColumn(name = "cohort_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<ApplicationUser> students = new HashSet<>();

    public String getDisplayName() {
        return year + "ano" + "-" + courseNameSnapshot + "-" + section + "-" + academicYear;
    }

    public int getStudentNumber() {
        return students.size();
    }
}

