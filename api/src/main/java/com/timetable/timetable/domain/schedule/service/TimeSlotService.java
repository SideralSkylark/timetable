package com.timetable.timetable.domain.schedule.service;

import com.timetable.timetable.domain.schedule.repository.TimeSlotRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeSlotService {
    private final TimeSlotRepository timeSlotRepository;
}
