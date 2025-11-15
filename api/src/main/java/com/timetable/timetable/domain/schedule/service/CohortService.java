package com.timetable.timetable.domain.schedule.service;

import com.timetable.timetable.domain.schedule.repository.CohortRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CohortService {
    private final CohortRepository cohortRepository;

    //TODO: crud
}
