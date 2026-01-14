package com.timetable.timetable.domain.schedule.entity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import com.timetable.timetable.domain.user.entity.ApplicationUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scheduled_classes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduledClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_subject_id", nullable = false)
    private CohortSubject cohortSubject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id") 
    private Timetable timetable;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;

    public Subject getSubject() {
        return cohortSubject.getSubject();
    }
    
    public ApplicationUser getTeacher() {
        return cohortSubject.getAssignedTeacher();
    }
    
    public Cohort getCohort() {
        return cohortSubject.getCohort();
    }

    @PrePersist
    @PreUpdate
    private void validateTimes() {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("TimeSlot must have valid start and end times.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
    }

    public int getDurationInMinutes() {
        return (int) Duration.between(startTime, endTime).toMinutes();
    }
}

