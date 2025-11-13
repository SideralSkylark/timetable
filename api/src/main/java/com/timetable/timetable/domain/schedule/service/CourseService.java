package com.timetable.timetable.domain.schedule.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final com.timetable.timetable.domain.schedule.repository.CourseRepository CourseRepository;
}
