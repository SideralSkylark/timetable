package com.timetable.timetable.scheduler_engine.mapper;

import com.timetable.timetable.domain.schedule.entity.*;
import com.timetable.timetable.scheduler_engine.domain.*;
import com.timetable.timetable.scheduler_engine.domain.info.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps between JPA persistence entities and solver domain objects.
 * 
 * Conversion flow:
 * - JPA entities → Solver domain (toPlanningProblem)
 * - Solver domain → JPA entities (toScheduledClasses)
 */
@Component
@Slf4j
public class TimetableSolutionMapper {
    
    // ========================================
    // MAIN MAPPING METHODS
    // ========================================
    
    /**
     * Converts JPA entities into a TimetableSolution ready for the solver.
     * 
     * This method:
     * 1. Converts all timeslots and rooms into Info objects (problem facts)
     * 2. For each CohortSubject, generates N empty LessonAssignments 
     *    (where N = blocks needed per week)
     * 3. Returns a TimetableSolution with unassigned planning variables
     * 
     * @param cohortSubjects Active cohort-subject combinations for this semester
     * @param timeslots Available time slots for scheduling
     * @param rooms Available rooms
     * @param academicYear The academic year (e.g., 2026)
     * @param semester The semester (1 or 2)
     * @return TimetableSolution ready to be solved
     */
    public TimetableSolution toPlanningProblem(
            List<CohortSubject> cohortSubjects,
            List<Timeslot> timeslots,
            List<Room> rooms,
            int academicYear,
            int semester) {
        
        log.info("Converting {} CohortSubjects, {} timeslots, {} rooms to planning problem",
            cohortSubjects.size(), timeslots.size(), rooms.size());
        
        // Convert problem facts (resources)
        List<TimeslotInfo> timeslotInfos = timeslots.stream()
            .map(this::toTimeslotInfo)
            .collect(Collectors.toList());
        
        List<RoomInfo> roomInfos = rooms.stream()
            .map(this::toRoomInfo)
            .collect(Collectors.toList());
        
        // Generate lesson assignments (planning entities)
        List<LessonAssignment> lessonAssignments = new ArrayList<>();
        long lessonIdCounter = 1;
        
        for (CohortSubject cs : cohortSubjects) {
            int blocksNeeded = cs.getLessonBlocksPerWeek();
            CohortSubjectInfo csInfo = toCohortSubjectInfo(cs);
            
            log.debug("CohortSubject #{}: {} needs {} blocks/week", 
                cs.getId(), csInfo.getDisplayName(), blocksNeeded);
            
            // Create one LessonAssignment for each block needed
            for (int blockNumber = 1; blockNumber <= blocksNeeded; blockNumber++) {
                LessonAssignment lesson = LessonAssignment.builder()
                    .id(lessonIdCounter++)
                    .cohortSubject(csInfo)
                    .blockNumber(blockNumber)
                    .timeslot(null) // To be assigned by solver
                    .room(null)     // To be assigned by solver
                    .build();
                
                lessonAssignments.add(lesson);
            }
        }
        
        log.info("Generated {} lesson assignments from {} cohort-subjects",
            lessonAssignments.size(), cohortSubjects.size());
        
        return TimetableSolution.builder()
            .lessonAssignments(lessonAssignments)
            .availableTimeslots(timeslotInfos)
            .availableRooms(roomInfos)
            .academicYear(academicYear)
            .semester(semester)
            .score(null) // Will be calculated by solver
            .build();
    }
    
    /**
     * Converts a solved TimetableSolution back into JPA ScheduledClass entities.
     * 
     * This method:
     * 1. Takes each LessonAssignment with assigned timeslot and room
     * 2. Looks up the corresponding JPA entities by ID
     * 3. Creates ScheduledClass entities linking everything together
     * 
     * @param solution The solved timetable
     * @param timetable The Timetable entity to associate with
     * @return List of ScheduledClass entities ready to be persisted
     */
    public List<ScheduledClass> toScheduledClasses(
            TimetableSolution solution,
            Timetable timetable) {
        
        if (solution == null || solution.getLessonAssignments() == null) {
            log.warn("Cannot convert null solution to ScheduledClasses");
            return new ArrayList<>();
        }
        
        log.info("Converting {} lesson assignments to ScheduledClass entities",
            solution.getLessonAssignments().size());
        
        // Note: In a real implementation, you would inject repositories here
        // and fetch the actual JPA entities by ID. For now, this creates
        // ScheduledClass objects with the IDs that need to be resolved.
        
        List<ScheduledClass> scheduledClasses = new ArrayList<>();
        
        for (LessonAssignment lesson : solution.getLessonAssignments()) {
            if (lesson.getTimeslot() == null || lesson.getRoom() == null) {
                log.warn("Skipping unassigned lesson: {}", lesson.getDisplayName());
                continue;
            }
            
            // Create ScheduledClass with IDs
            // In real implementation, replace with:
            // cohortSubjectRepo.getReferenceById(lesson.getCohortSubject().getId())
            // timeslotRepo.getReferenceById(lesson.getTimeslot().getId())
            // roomRepo.getReferenceById(lesson.getRoom().getId())
            
            ScheduledClass scheduledClass = ScheduledClass.builder()
                .timetable(timetable)
                // .cohortSubject(cohortSubjectRepo.getReferenceById(...))
                // .timeslot(timeslotRepo.getReferenceById(...))
                // .room(roomRepo.getReferenceById(...))
                .build();
            
            // Store IDs as metadata for now (you'll replace this)
            // scheduledClass.setCohortSubject(...);
            // scheduledClass.setTimeslot(...);
            // scheduledClass.setRoom(...);
            
            scheduledClasses.add(scheduledClass);
        }
        
        log.info("Created {} ScheduledClass entities", scheduledClasses.size());
        
        return scheduledClasses;
    }
    
    // ========================================
    // ENTITY → INFO CONVERSIONS
    // ========================================
    
    private CohortSubjectInfo toCohortSubjectInfo(CohortSubject cs) {
        return CohortSubjectInfo.builder()
            .id(cs.getId())
            .cohort(toCohortInfo(cs.getCohort()))
            .subject(toSubjectInfo(cs.getSubject()))
            .teacher(toTeacherInfo(cs.getAssignedTeacher()))
            .lessonBlocksPerWeek(cs.getLessonBlocksPerWeek())
            .build();
    }
    
    private CohortInfo toCohortInfo(Cohort cohort) {
        return CohortInfo.builder()
            .id(cohort.getId())
            .displayName(cohort.getDisplayName())
            .studentCount(cohort.getStudentCount())
            .courseId(cohort.getCourse().getId())
            .year(cohort.getYear())
            .section(cohort.getSection())
            .build();
    }
    
    private SubjectInfo toSubjectInfo(Subject subject) {
        return SubjectInfo.builder()
            .id(subject.getId())
            .name(subject.getName())
            .credits(subject.getCredits())
            .targetYear(subject.getTargetYear())
            .targetSemester(subject.getTargetSemester())
            .build();
    }
    
    private TeacherInfo toTeacherInfo(com.timetable.timetable.domain.user.entity.ApplicationUser teacher) {
        return TeacherInfo.builder()
            .id(teacher.getId())
            .name(teacher.getUsername())
            .fullName(teacher.getUsername())
            .email(teacher.getEmail())
            .build();
    }
    
    private TimeslotInfo toTimeslotInfo(Timeslot timeslot) {
        return TimeslotInfo.builder()
            .id(timeslot.getId())
            .dayOfWeek(timeslot.getDayOfWeek())
            .startTime(timeslot.getStartTime())
            .endTime(timeslot.getEndTime())
            .build();
    }
    
    private RoomInfo toRoomInfo(Room room) {
        Map<TimePeriod, Set<Long>> restrictionsMap = new HashMap<>();
        
        if (room.getRestrictions() != null && !room.getRestrictions().isEmpty()) {
            for (RoomCourseRestriction restriction : room.getRestrictions()) {
                restrictionsMap
                    .computeIfAbsent(restriction.getPeriod(), k -> new HashSet<>())
                    .add(restriction.getCourse().getId());
            }
        }

        return RoomInfo.builder()
            .id(room.getId())
            .name(room.getName())
            .capacity(room.getCapacity())
            .allowedCoursesByPeriod(restrictionsMap.isEmpty() ? null : restrictionsMap)
            .build();
    }
}
