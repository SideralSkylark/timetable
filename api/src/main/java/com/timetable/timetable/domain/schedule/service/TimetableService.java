package com.timetable.timetable.domain.schedule.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CreateTimetableRequest;
import com.timetable.timetable.domain.schedule.dto.TimetableResponse;
import com.timetable.timetable.domain.schedule.dto.UpdateTimetableRequest;
import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.schedule.entity.TimetableStatus;
import com.timetable.timetable.domain.schedule.exception.TimetableNotFoundException;
import com.timetable.timetable.domain.schedule.repository.TimetableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimetableService {
    private final TimetableRepository timetableRepository;

    @Transactional
    public TimetableResponse createTimetable(CreateTimetableRequest createRequest) {
        // Check if timetable for this academic period already exists
        if (timetableRepository.existsByAcademicPeriod(createRequest.academicPeriod())) {
            throw new IllegalStateException(
                "Timetable for academic period '%s' already exists".formatted(createRequest.academicPeriod())
            );
        }

        Timetable timetable = Timetable.builder()
            .academicPeriod(createRequest.academicPeriod())
            .status(TimetableStatus.DRAFT)
            .build();

        Timetable saved = timetableRepository.save(timetable);
        return TimetableResponse.from(saved);
    }

    public Page<TimetableResponse> getAll(Pageable pageable) {
        return timetableRepository.findAll(pageable)
            .map(TimetableResponse::from);
    }

    public Page<TimetableResponse> getByStatus(TimetableStatus status, Pageable pageable) {
        return timetableRepository.findByStatus(status, pageable)
            .map(TimetableResponse::from);
    }

    public TimetableResponse getById(Long id) {
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));
        return TimetableResponse.from(timetable);
    }

    public Timetable getTimetableById(Long id) {
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));
        return timetable;
    }

    public TimetableResponse getByAcademicPeriod(String academicPeriod) {
        Timetable timetable = timetableRepository.findByAcademicPeriod(academicPeriod)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable for academic period '%s' not found".formatted(academicPeriod)
            ));
        return TimetableResponse.from(timetable);
    }

    @Transactional
    public TimetableResponse updateTimetable(Long id, UpdateTimetableRequest updateRequest) {
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        // Check if trying to change to a different academic period that already exists
        if (!timetable.getAcademicPeriod().equals(updateRequest.academicPeriod()) && 
            timetableRepository.existsByAcademicPeriod(updateRequest.academicPeriod())) {
            throw new IllegalArgumentException(
                "Another timetable for academic period '%s' already exists".formatted(updateRequest.academicPeriod())
            );
        }

        timetable.setAcademicPeriod(updateRequest.academicPeriod());
        timetable.setStatus(updateRequest.status());

        Timetable updated = timetableRepository.save(timetable);
        return TimetableResponse.from(updated);
    }

    @Transactional
    public TimetableResponse publishTimetable(Long id) {
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        if (timetable.getStatus() == TimetableStatus.PUBLISHED) {
            throw new IllegalStateException(
                "Timetable is already published"
            );
        }

        if (timetable.getTimeSlots() == null || timetable.getTimeSlots().isEmpty()) {
            throw new IllegalStateException(
                "Cannot publish an empty timetable. Please add time slots first"
            );
        }

        timetable.setStatus(TimetableStatus.PUBLISHED);
        Timetable updated = timetableRepository.save(timetable);
        return TimetableResponse.from(updated);
    }

    @Transactional
    public TimetableResponse archiveTimetable(Long id) {
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        if (timetable.getStatus() == TimetableStatus.ARCHIVED) {
            throw new IllegalStateException(
                "Timetable is already archived"
            );
        }

        timetable.setStatus(TimetableStatus.ARCHIVED);
        Timetable updated = timetableRepository.save(timetable);
        return TimetableResponse.from(updated);
    }

    @Transactional
    public TimetableResponse revertToDraft(Long id) {
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        if (timetable.getStatus() == TimetableStatus.DRAFT) {
            throw new IllegalStateException(
                "Timetable is already in draft status"
            );
        }

        if (timetable.getStatus() == TimetableStatus.ARCHIVED) {
            throw new IllegalStateException(
                "Cannot revert an archived timetable to draft. Please create a new timetable"
            );
        }

        timetable.setStatus(TimetableStatus.DRAFT);
        Timetable updated = timetableRepository.save(timetable);
        return TimetableResponse.from(updated);
    }

    @Transactional
    public void deleteTimetable(Long id) {
        Timetable timetable = timetableRepository.findById(id)
            .orElseThrow(() -> new TimetableNotFoundException(
                "Timetable with id %d not found".formatted(id)
            ));

        if (timetable.getStatus() == TimetableStatus.PUBLISHED) {
            throw new IllegalStateException(
                "Cannot delete a published timetable. Please archive it first"
            );
        }

        timetableRepository.deleteById(id);
    }
}
