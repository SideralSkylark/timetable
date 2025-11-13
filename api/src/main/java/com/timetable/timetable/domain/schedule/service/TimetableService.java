package com.timetable.timetable.domain.schedule.service;

import com.timetable.timetable.domain.schedule.repository.TimetableRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimetableService {
    private final TimetableRepository timetableRepository;
}
