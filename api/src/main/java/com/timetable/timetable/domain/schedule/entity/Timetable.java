package com.timetable.timetable.domain.schedule.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "timetables")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduledClass> timeSlots;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private TimetableStatus status = TimetableStatus.DRAFT;

    private String academicPeriod; // ex: "2024.1", "Fall 2024"
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

