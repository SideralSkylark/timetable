package com.timetable.timetable.domain.schedule.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CreateTimeSlotRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateTimeSlotRequest;
import com.timetable.timetable.domain.schedule.entity.Cohort;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.Subject;
import com.timetable.timetable.domain.schedule.entity.TimeSlot;
import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.schedule.exception.TimeSlotNotFoundException;
import com.timetable.timetable.domain.schedule.repository.TimeSlotRepository;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimeSlotService {
    private final TimeSlotRepository timeSlotRepository;
    private final SubjectService subjectService;
    private final TimetableService timetableService;
    private final UserService userService;
    private final RoomService roomService;
    private final CohortService cohortService;

    @Transactional
    public TimeSlot createTimeSlot(CreateTimeSlotRequest createRequest) {
        log.debug("Creating timeslot");
        Subject subject = subjectService.getById(createRequest.subjectId());

        Timetable timetable = null;
        if (createRequest.timetableId() != null) {
            timetable = timetableService.getById(createRequest.timetableId());
        }

        ApplicationUser teacher = userService.getUserById(createRequest.teacherId());
        
        if (!teacher.hasRole(UserRole.TEACHER)) {
            log.warn("User {} is not a teacher", teacher.getId());
            throw new IllegalArgumentException(
                "User with id %d is not a teacher".formatted(createRequest.teacherId())
            );
        }

        // Validate teacher teaches this subject
        if (!subject.getTeachers().contains(teacher)) {
            log.warn("Teacher {} is not assigned to subject {}", teacher.getId(), subject.getId());
            throw new IllegalArgumentException(
                "Teacher with id %d is not assigned to subject '%s'".formatted(
                    createRequest.teacherId(), subject.getName()
                )
            );
        }

        Room room = roomService.getById(createRequest.roomId());

        Cohort cohort = cohortService.getById(createRequest.cohortId());

        // Validate time constraints
        validateTimeConstraints(createRequest.startTime(), createRequest.endTime());

        // Check for conflicts
        checkForConflicts(
            null,
            teacher,
            room,
            cohort,
            createRequest.date(),
            createRequest.startTime(),
            createRequest.endTime()
        );

        TimeSlot timeSlot = TimeSlot.builder()
            .subject(subject)
            .timetable(timetable)
            .teacher(teacher)
            .room(room)
            .cohort(cohort)
            .date(createRequest.date())
            .startTime(createRequest.startTime())
            .endTime(createRequest.endTime())
            .build();

        TimeSlot saved = timeSlotRepository.save(timeSlot);

        log.info("Timeslot {} created", saved.getId());
        return saved;
    }

    public Page<TimeSlot> getAll(Pageable pageable) {
        log.debug("Fetching all timeslots");
        return timeSlotRepository.findAll(pageable);
    }

    public Page<TimeSlot> getByTimetable(Long timetableId, Pageable pageable) {
        log.debug("Fetching timeslot by timetable {}", timetableId);
        Timetable timetable = timetableService.getById(timetableId);
        
        log.info("timeslot, found for timetable {}", timetableId);
        return timeSlotRepository.findByTimetable(timetable, pageable);
    }

    public Page<TimeSlot> getByCohort(Long cohortId, Pageable pageable) {
        log.debug("Fetching timeslot by cohort {}", cohortId);
        Cohort cohort = cohortService.getById(cohortId);
        
        log.info("Found timeslot for cohort {}", cohortId);
        return timeSlotRepository.findByCohort(cohort, pageable);
    }

    public Page<TimeSlot> getByTeacher(Long teacherId, Pageable pageable) {
        log.debug("Fetching timeslot by teacher {}", teacherId);
        ApplicationUser teacher = userService.getUserById(teacherId);
        
        if (!teacher.hasRole(UserRole.TEACHER)) {
            log.warn("User {} is not a teacher", teacherId);
            throw new IllegalArgumentException(
                "User with id %d is not a teacher".formatted(teacherId)
            );
        }
        
        log.info("Found timeslot by teacher {}", teacherId);
        return timeSlotRepository.findByTeacher(teacher, pageable);
    }

    public TimeSlot getById(Long id) {
        log.debug("Fetching timeslot {}", id);
        TimeSlot timeSlot = timeSlotRepository.findById(id)
            .orElseThrow(() -> new TimeSlotNotFoundException(
                "Time slot with id %d not found".formatted(id)
            ));

        log.info("Timeslot {} found", timeSlot.getId());
        return timeSlot;
    }

    @Transactional
    public TimeSlot updateTimeSlot(Long id, UpdateTimeSlotRequest updateRequest) {
        log.debug("Updating timeslot {}", id);
        TimeSlot timeSlot = timeSlotRepository.findById(id)
            .orElseThrow(() -> new TimeSlotNotFoundException(
                "Time slot with id %d not found".formatted(id)
            ));

        // Validate all entities exist
        Subject subject = subjectService.getById(updateRequest.subjectId());

        ApplicationUser teacher = userService.getUserById(updateRequest.teacherId());
        
        if (!teacher.hasRole(UserRole.TEACHER)) {
            log.warn("User {} is not a teacher", teacher.getId());
            throw new IllegalArgumentException(
                "User with id %d is not a teacher".formatted(updateRequest.teacherId())
            );
        }

        // Validate teacher teaches this subject
        if (!subject.getTeachers().contains(teacher)) {
            log.warn("Teacher {} does not teach this subject", teacher.getId());
            throw new IllegalArgumentException(
                "Teacher with id %d is not assigned to subject '%s'".formatted(
                    updateRequest.teacherId(), subject.getName()
                )
            );
        }

        Room room = roomService.getById(updateRequest.roomId());

        Cohort cohort = cohortService.getById(updateRequest.cohortId());

        // Validate time constraints
        validateTimeConstraints(updateRequest.startTime(), updateRequest.endTime());

        // Check for conflicts (excluding current time slot)
        checkForConflicts(
            id,
            teacher,
            room,
            cohort,
            updateRequest.date(),
            updateRequest.startTime(),
            updateRequest.endTime()
        );

        timeSlot.setSubject(subject);
        timeSlot.setTeacher(teacher);
        timeSlot.setRoom(room);
        timeSlot.setCohort(cohort);
        timeSlot.setDate(updateRequest.date());
        timeSlot.setStartTime(updateRequest.startTime());
        timeSlot.setEndTime(updateRequest.endTime());

        TimeSlot updated = timeSlotRepository.save(timeSlot);

        log.info("Timeslot {} updated", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteTimeSlot(Long id) {
        log.debug("Deleting timeslot {}", id);
        if (!timeSlotRepository.existsById(id)) {
            log.warn("Timeslot {} not found", id);
            throw new TimeSlotNotFoundException(
                "Time slot with id %d not found".formatted(id)
            );
        }

        timeSlotRepository.deleteById(id);
        log.info("Timeslot {} deleted", id);
    }

    private void validateTimeConstraints(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                "Start time must be before end time"
            );
        }
    }

    private void checkForConflicts(
        Long excludeTimeSlotId,
        ApplicationUser teacher,
        Room room,
        Cohort cohort,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
    ) {
        // Check teacher availability
        List<TimeSlot> teacherConflicts = timeSlotRepository
            .findByTeacherAndDateAndTimeOverlap(teacher, date, startTime, endTime);
        
        if (excludeTimeSlotId != null) {
            teacherConflicts.removeIf(slot -> slot.getId().equals(excludeTimeSlotId));
        }
        
        if (!teacherConflicts.isEmpty()) {
            throw new IllegalArgumentException(
                "Teacher is already scheduled for another class at this time"
            );
        }

        // Check room availability
        List<TimeSlot> roomConflicts = timeSlotRepository
            .findByRoomAndDateAndTimeOverlap(room, date, startTime, endTime);
        
        if (excludeTimeSlotId != null) {
            roomConflicts.removeIf(slot -> slot.getId().equals(excludeTimeSlotId));
        }
        
        if (!roomConflicts.isEmpty()) {
            throw new IllegalArgumentException(
                "Room is already booked for another class at this time"
            );
        }

        // Check cohort availability
        List<TimeSlot> cohortConflicts = timeSlotRepository
            .findByCohortAndDateAndTimeOverlap(cohort, date, startTime, endTime);
        
        if (excludeTimeSlotId != null) {
            cohortConflicts.removeIf(slot -> slot.getId().equals(excludeTimeSlotId));
        }
        
        if (!cohortConflicts.isEmpty()) {
            throw new IllegalArgumentException(
                "Cohort already has a class scheduled at this time"
            );
        }
    }
}
