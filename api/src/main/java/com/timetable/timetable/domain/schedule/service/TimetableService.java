package com.timetable.timetable.domain.schedule.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CreateTimetableRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateTimetableRequest;
import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.schedule.entity.TimetableStatus;
import com.timetable.timetable.domain.schedule.exception.TimetableNotFoundException;
import com.timetable.timetable.domain.schedule.repository.TimetableRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimetableService {
    private final TimetableRepository timetableRepository;

    @Transactional
    public Timetable createTimetable(CreateTimetableRequest createRequest) {
        log.debug("Creating timetable");
        if (timetableRepository.existsByAcademicYearAndSemester(createRequest.academicYear(), createRequest.semester())) {
            log.warn("Timetable for period {} already exists", createRequest.academicYear());
            throw new IllegalStateException(
                "Timetable for academic period '%s' already exists".formatted(createRequest.academicYear())
            );
        }

        Timetable timetable = Timetable.builder()
            .academicYear(createRequest.academicYear())
            .semester(createRequest.semester())
            .status(TimetableStatus.DRAFT)
            .build();

        Timetable saved = timetableRepository.save(timetable);

        log.info("Timetable {} created", saved.getId());
        return saved;
    }

    public Page<Timetable> getAll(Pageable pageable) {
        log.debug("Fetching all timetables");
        return timetableRepository.findAll(pageable);
    }

    public Page<Timetable> getByStatus(TimetableStatus status, Pageable pageable) {
        log.debug("Fetching all {} timetables", status);
        return timetableRepository.findByStatus(status, pageable);
    }

    public Timetable getById(Long id) {
        log.debug("fetching timetable {}", id);
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        log.info("timetable {} found", timetable.getId());
        return timetable;
    }

    public Timetable getByAcademicPeriod(int academicYear, int semester) {
        log.debug("fetching timetable by {} year and {} semester", academicYear, semester);
        Timetable timetable = timetableRepository.findByAcademicYearAndSemester(academicYear, semester)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable for academic period '%s' not found".formatted(academicYear + "." + semester)
            ));

        log.info("timetable {} found", timetable.getId());
        return timetable;
    }

    @Transactional
    public Timetable updateTimetable(Long id, UpdateTimetableRequest updateRequest) {
        log.debug("updating timetable {}", id);
        Timetable timetable = getById(id);

        // Check if trying to change to a different academic period that already exists
        if (!timetable.getAcademicPeriod().equals(updateRequest.academicYear() + "." + updateRequest.semester()) && 
            timetableRepository.existsByAcademicYearAndSemester(updateRequest.academicYear(), updateRequest.semester())) {
            log.warn("another timetable for {} period already exists", updateRequest.academicYear());
            throw new IllegalArgumentException(
                "Another timetable for academic period '%s' already exists".formatted(updateRequest.academicYear())
            );
        }

        timetable.setAcademicYear(updateRequest.academicYear());
        timetable.setSemester(updateRequest.semester());
        timetable.setStatus(updateRequest.status());

        Timetable updated = timetableRepository.save(timetable);

        log.info("timetable {} updated", updated.getId());
        return updated;
    }

    @Transactional
    public Timetable publishTimetable(Long id) {
        log.debug("publishing timetable");
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        if (timetable.getStatus() == TimetableStatus.PUBLISHED) {
            log.warn("timetable {} is already published", id);
            throw new IllegalStateException(
                "Timetable is already published"
            );
        }

        if (timetable.getScheduledClasses() == null || timetable.getScheduledClasses().isEmpty()) {
            log.warn("cannot publish an empty timetable");
            throw new IllegalStateException(
                "Cannot publish an empty timetable. Please add time slots first"
            );
        }

        timetable.setStatus(TimetableStatus.PUBLISHED);
        Timetable updated = timetableRepository.save(timetable);

        log.info("timetable {} updated", updated.getId());
        return updated;
    }

    @Transactional
    public Timetable archiveTimetable(Long id) {
        log.debug("archiving timetable {}", id);
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        if (timetable.getStatus() == TimetableStatus.ARCHIVED) {
            log.warn("timetable {} already arquived", timetable.getId());
            throw new IllegalStateException(
                "Timetable is already archived"
            );
        }

        timetable.setStatus(TimetableStatus.ARCHIVED);
        Timetable updated = timetableRepository.save(timetable);

        log.info("timetable {} arquived", timetable.getId());
        return updated;
    }

    @Transactional
    public Timetable revertToDraft(Long id) {
        log.debug("Reverting timetable {} to draft", id);
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        if (timetable.getStatus() == TimetableStatus.DRAFT) {
            log.debug("timetable {} is already in draft status", timetable.getId());
            throw new IllegalStateException(
                "Timetable is already in draft status"
            );
        }

        if (timetable.getStatus() == TimetableStatus.ARCHIVED) {
            log.debug("cannot revert an archived timetable");
            throw new IllegalStateException(
                "Cannot revert an archived timetable to draft. Please create a new timetable"
            );
        }

        timetable.setStatus(TimetableStatus.DRAFT);
        Timetable updated = timetableRepository.save(timetable);

        log.info("timetable {} updated", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteTimetable(Long id) {
        log.debug("deleting timetable {}", id);
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        if (timetable.getStatus() == TimetableStatus.PUBLISHED) {
            log.debug("cannot deleted a published timetable, please archive it first");
            throw new IllegalStateException(
                "Cannot delete a published timetable. Please archive it first"
            );
        }

        timetableRepository.deleteById(id);
        log.info("deleted timetable {}", id);
    }
}
