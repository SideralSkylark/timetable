package com.timetable.timetable.domain.schedule.query;

import com.timetable.timetable.domain.schedule.dto.CohortFilterParams;
import com.timetable.timetable.domain.schedule.dto.CohortListResponse;
import com.timetable.timetable.domain.schedule.repository.CohortRepository;
import com.timetable.timetable.domain.schedule.specification.CohortSpecifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CohortQueryService {

    private final CohortRepository cohortRepository;

    @Transactional
    public Page<CohortListResponse> findAll(Pageable pageable, CohortFilterParams filters) {

        return cohortRepository.findAll(CohortSpecifications.withFilters(filters), pageable)
                .map(cohort -> new CohortListResponse(
                        cohort.getId(),
                        cohort.getYear(),
                        cohort.getSection(),
                        cohort.getAcademicYear(),
                        cohort.getSemester(),
                        cohort.getCourse().getId(),
                        cohort.getCourseNameSnapshot(),
                        cohort.getStudentCount(),
                        cohort.getStatus()
                ));
    }
}
