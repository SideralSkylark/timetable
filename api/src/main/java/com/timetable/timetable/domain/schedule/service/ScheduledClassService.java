package com.timetable.timetable.domain.schedule.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CreateScheduledClassRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateScheduledClassRequest;
import com.timetable.timetable.domain.schedule.entity.CohortSubject;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.ScheduledClass;
import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.schedule.exception.ScheduledClassNotFoundException;
import com.timetable.timetable.domain.schedule.repository.ScheduledClassRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledClassService {
    private final ScheduledClassRepository scheduledClassRepository;
    private final CohortSubjectService cohortSubjectService;
    private final TimetableService timetableService;
    private final RoomService roomService;

    @Transactional
    public ScheduledClass createScheduledClass(CreateScheduledClassRequest request) {
        log.debug("Creating scheduled class");
        
        // Buscar entidades relacionadas
        CohortSubject cohortSubject = cohortSubjectService.getById(request.cohortSubjectId());
        Room room = roomService.getById(request.roomId());
        Timetable timetable = null;
        
        if (request.timetableId() != null) {
            timetable = timetableService.getById(request.timetableId());
        }
        
        // Validar que o CohortSubject está ativo
        if (!cohortSubject.isActive()) {
            throw new IllegalStateException(
                "Cannot schedule a class for an inactive cohort subject assignment"
            );
        }
        
        // Validar datas e horários
        validateScheduleConstraints(
            cohortSubject, 
            request.date(), 
            request.startTime(), 
            request.endTime()
        );
        
        // Verificar conflitos
        checkForConflicts(
            null,
            cohortSubject,
            room,
            request.date(),
            request.startTime(),
            request.endTime()
        );
        
        // Criar a aula agendada
        ScheduledClass scheduledClass = ScheduledClass.builder()
            .cohortSubject(cohortSubject)
            .timetable(timetable)
            .room(room)
            .date(request.date())
            .startTime(request.startTime())
            .endTime(request.endTime())
            .build();
        
        ScheduledClass saved = scheduledClassRepository.save(scheduledClass);
        
        log.info("Scheduled class {} created for {}", 
            saved.getId(), cohortSubject.getDisplayName());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<ScheduledClass> getAll(Pageable pageable) {
        log.debug("Fetching all scheduled classes");
        return scheduledClassRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ScheduledClass> getByTimetable(Long timetableId, Pageable pageable) {
        log.debug("Fetching scheduled classes for timetable {}", timetableId);
        Timetable timetable = timetableService.getById(timetableId);
        return scheduledClassRepository.findByTimetable(timetable, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ScheduledClass> getByCohort(Long cohortId, Pageable pageable) {
        log.debug("Fetching scheduled classes for cohort {}", cohortId);
        return scheduledClassRepository.findByCohortId(cohortId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ScheduledClass> getByTeacher(Long teacherId, Pageable pageable) {
        log.debug("Fetching scheduled classes for teacher {}", teacherId);
        return scheduledClassRepository.findByTeacherId(teacherId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ScheduledClass> getByCohortSubject(Long cohortSubjectId, Pageable pageable) {
        log.debug("Fetching scheduled classes for cohort subject {}", cohortSubjectId);
        return scheduledClassRepository.findByCohortSubjectId(cohortSubjectId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ScheduledClass> getByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Fetching scheduled classes between {} and {}", startDate, endDate);
        return scheduledClassRepository.findByDateBetween(startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public ScheduledClass getById(Long id) {
        log.debug("Fetching scheduled class {}", id);
        return scheduledClassRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ScheduledClassNotFoundException(
                String.format("Scheduled class with id %d not found", id)
            ));
    }

    @Transactional
    public ScheduledClass updateScheduledClass(Long id, UpdateScheduledClassRequest request) {
        log.debug("Updating scheduled class {}", id);
        ScheduledClass scheduledClass = getById(id);
        
        // Buscar novas entidades se necessário
        CohortSubject cohortSubject = scheduledClass.getCohortSubject();
        Room room = scheduledClass.getRoom();
        
        if (!request.cohortSubjectId().equals(cohortSubject.getId())) {
            cohortSubject = cohortSubjectService.getById(request.cohortSubjectId());
            scheduledClass.setCohortSubject(cohortSubject);
        }
        
        if (!request.roomId().equals(room.getId())) {
            room = roomService.getById(request.roomId());
            scheduledClass.setRoom(room);
        }
        
        Timetable timetable = scheduledClass.getTimetable();
        if (request.timetableId() != null && 
            (timetable == null || !request.timetableId().equals(timetable.getId()))) {
            timetable = timetableService.getById(request.timetableId());
            scheduledClass.setTimetable(timetable);
        } else if (request.timetableId() == null) {
            scheduledClass.setTimetable(null);
        }
        
        // Validar que o CohortSubject está ativo
        if (!cohortSubject.isActive()) {
            throw new IllegalStateException(
                "Cannot schedule a class for an inactive cohort subject assignment"
            );
        }
        
        // Validar datas e horários
        validateScheduleConstraints(
            cohortSubject,
            request.date(),
            request.startTime(),
            request.endTime()
        );
        
        // Verificar conflitos (excluindo esta aula)
        checkForConflicts(
            id,
            cohortSubject,
            room,
            request.date(),
            request.startTime(),
            request.endTime()
        );
        
        // Atualizar campos
        scheduledClass.setDate(request.date());
        scheduledClass.setStartTime(request.startTime());
        scheduledClass.setEndTime(request.endTime());
        
        ScheduledClass updated = scheduledClassRepository.save(scheduledClass);
        
        log.info("Scheduled class {} updated", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteScheduledClass(Long id) {
        log.debug("Deleting scheduled class {}", id);
        if (!scheduledClassRepository.existsById(id)) {
            throw new ScheduledClassNotFoundException(
                String.format("Scheduled class with id %d not found", id)
            );
        }
        
        scheduledClassRepository.deleteById(id);
        log.info("Scheduled class {} deleted", id);
    }

    private void validateScheduleConstraints(
        CohortSubject cohortSubject,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
    ) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
        
        // Validar que a data está dentro do ano/semestre acadêmico
        // (implementar conforme as regras da instituição)
        validateAcademicDate(date, cohortSubject.getAcademicYear(), cohortSubject.getSemester());
        
        // Validar duração mínima/máxima da aula
        int durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes < 30) {
            throw new IllegalArgumentException("Class duration must be at least 30 minutes");
        }
        if (durationMinutes > 240) { // 4 horas
            throw new IllegalArgumentException("Class duration cannot exceed 4 hours");
        }
    }

    private void checkForConflicts(
        Long excludeScheduledClassId,
        CohortSubject cohortSubject,
        Room room,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
    ) {
        // Verificar conflitos de professor
        List<ScheduledClass> teacherConflicts = scheduledClassRepository
            .findByTeacherAndDateAndTimeOverlap(
                cohortSubject.getAssignedTeacher(), 
                date, startTime, endTime
            );
        
        if (excludeScheduledClassId != null) {
            teacherConflicts.removeIf(sc -> sc.getId().equals(excludeScheduledClassId));
        }
        
        if (!teacherConflicts.isEmpty()) {
            throw new IllegalArgumentException(
                "Teacher is already scheduled for another class at this time"
            );
        }
        
        // Verificar conflitos de sala
        List<ScheduledClass> roomConflicts = scheduledClassRepository
            .findByRoomAndDateAndTimeOverlap(room, date, startTime, endTime);
        
        if (excludeScheduledClassId != null) {
            roomConflicts.removeIf(sc -> sc.getId().equals(excludeScheduledClassId));
        }
        
        if (!roomConflicts.isEmpty()) {
            throw new IllegalArgumentException(
                "Room is already booked for another class at this time"
            );
        }
        
        // Verificar conflitos de turma
        List<ScheduledClass> cohortConflicts = scheduledClassRepository
            .findByCohortAndDateAndTimeOverlap(
                cohortSubject.getCohort(), 
                date, startTime, endTime
            );
        
        if (excludeScheduledClassId != null) {
            cohortConflicts.removeIf(sc -> sc.getId().equals(excludeScheduledClassId));
        }
        
        if (!cohortConflicts.isEmpty()) {
            throw new IllegalArgumentException(
                "Cohort already has a class scheduled at this time"
            );
        }
    }

    private void validateAcademicDate(LocalDate date, int academicYear, int semester) {
        // Implementar lógica específica da instituição
        // Exemplo: verificar se a data está dentro do semestre acadêmico
        
        // Para simplificar, apenas verificar o ano
        if (date.getYear() != academicYear) {
            log.warn("Scheduled class date ({}) does not match academic year ({})", 
                date.getYear(), academicYear);
            // Pode ser um aviso em vez de erro, dependendo das regras
        }
    }
    
    @Transactional(readOnly = true)
    public List<ScheduledClass> getWeeklySchedule(Long cohortId, LocalDate weekStartDate) {
        log.debug("Fetching weekly schedule for cohort {} starting {}", cohortId, weekStartDate);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        return scheduledClassRepository.findByCohortIdAndDateBetween(
            cohortId, weekStartDate, weekEndDate);
    }
    
    @Transactional(readOnly = true)
    public int countScheduledClassesForCohortSubject(Long cohortSubjectId) {
        return scheduledClassRepository.countByCohortSubjectId(cohortSubjectId);
    }
}
