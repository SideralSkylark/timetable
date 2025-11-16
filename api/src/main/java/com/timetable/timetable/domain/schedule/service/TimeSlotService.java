package com.timetable.timetable.domain.schedule.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CreateTimeSlotRequest;
import com.timetable.timetable.domain.schedule.dto.TimeSlotResponse;
import com.timetable.timetable.domain.schedule.dto.UpdateTimeSlotRequest;
import com.timetable.timetable.domain.schedule.entity.Cohort;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.Subject;
import com.timetable.timetable.domain.schedule.entity.TimeSlot;
import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.schedule.repository.CohortRepository;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;
import com.timetable.timetable.domain.schedule.repository.SubjectRepository;
import com.timetable.timetable.domain.schedule.repository.TimeSlotRepository;
import com.timetable.timetable.domain.schedule.repository.TimetableRepository;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.repository.UserRepository;
import com.timetable.timetable.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeSlotService {
    private final TimeSlotRepository timeSlotRepository;
    private final SubjectService subjectService;
    private final TimetableService timetableService;
    private final UserService userService;
    private final RoomService roomService;
    private final CohortService cohortService;

    // private final SubjectRepository subjectRepository;
    // private final TimetableRepository timetableRepository;
    // private final UserRepository userRepository;
    // private final RoomRepository roomRepository;
    // private final CohortRepository cohortRepository;

    @Transactional
    public TimeSlotResponse createTimeSlot(CreateTimeSlotRequest createRequest) {
        Subject subject = subjectService.getSubjectById(createRequest.subjectId());

        Timetable timetable = null;
        if (createRequest.timetableId() != null) {
            timetable = timetableService.getTimetableById(createRequest.timetableId());
        }

        ApplicationUser teacher = userService.findById(createRequest.teacherId())
            .orElseThrow(() -> new IllegalArgumentException(
                "User with id %d not found".formatted(createRequest.teacherId())
            ));
        
        if (!teacher.hasRole(UserRole.TEACHER)) {
            throw new IllegalArgumentException(
                "User with id %d is not a teacher".formatted(createRequest.teacherId())
            );
        }

        // Validate teacher teaches this subject
        if (!subject.getTeachers().contains(teacher)) {
            throw new IllegalArgumentException(
                "Teacher with id %d is not assigned to subject '%s'".formatted(
                    createRequest.teacherId(), subject.getName()
                )
            );
        }

        Room room = roomService.getRoomById(createRequest.roomId());

        Cohort cohort = cohortService.getCohortById(createRequest.cohortId());

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
        return TimeSlotResponse.from(saved);
    }

    public Page<TimeSlotResponse> getAll(Pageable pageable) {
        return timeSlotRepository.findAll(pageable)
            .map(TimeSlotResponse::from);
    }

    public Page<TimeSlotResponse> getByTimetable(Long timetableId, Pageable pageable) {
        Timetable timetable = timetableService.getTimetableById(timetableId);
        
        return timeSlotRepository.findByTimetable(timetable, pageable)
            .map(TimeSlotResponse::from);
    }

    public Page<TimeSlotResponse> getByCohort(Long cohortId, Pageable pageable) {
        Cohort cohort = cohortService.getCohortById(cohortId);
        
        return timeSlotRepository.findByCohort(cohort, pageable)
            .map(TimeSlotResponse::from);
    }

    public Page<TimeSlotResponse> getByTeacher(Long teacherId, Pageable pageable) {
        ApplicationUser teacher = userService.findById(teacherId)
            .orElseThrow(() -> new IllegalArgumentException(
                "User with id %d not found".formatted(teacherId)
            ));
        
        if (!teacher.hasRole(UserRole.TEACHER)) {
            throw new IllegalArgumentException(
                "User with id %d is not a teacher".formatted(teacherId)
            );
        }
        
        return timeSlotRepository.findByTeacher(teacher, pageable)
            .map(TimeSlotResponse::from);
    }

    public TimeSlotResponse getById(Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Time slot with id %d not found".formatted(id)
            ));
        return TimeSlotResponse.from(timeSlot);
    }

    @Transactional
    public TimeSlotResponse updateTimeSlot(Long id, UpdateTimeSlotRequest updateRequest) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Time slot with id %d not found".formatted(id)
            ));

        // Validate all entities exist
        Subject subject = subjectService.getSubjectById(updateRequest.subjectId());

        ApplicationUser teacher = userService.findById(updateRequest.teacherId())
            .orElseThrow(() -> new IllegalArgumentException(
                "User with id %d not found".formatted(updateRequest.teacherId())
            ));
        
        if (!teacher.hasRole(UserRole.TEACHER)) {
            throw new IllegalArgumentException(
                "User with id %d is not a teacher".formatted(updateRequest.teacherId())
            );
        }

        // Validate teacher teaches this subject
        if (!subject.getTeachers().contains(teacher)) {
            throw new IllegalArgumentException(
                "Teacher with id %d is not assigned to subject '%s'".formatted(
                    updateRequest.teacherId(), subject.getName()
                )
            );
        }

        Room room = roomService.getRoomById(updateRequest.roomId());

        Cohort cohort = cohortService.getCohortById(updateRequest.cohortId());

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
        return TimeSlotResponse.from(updated);
    }

    @Transactional
    public void deleteTimeSlot(Long id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "Time slot with id %d not found".formatted(id)
            );
        }
        timeSlotRepository.deleteById(id);
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
