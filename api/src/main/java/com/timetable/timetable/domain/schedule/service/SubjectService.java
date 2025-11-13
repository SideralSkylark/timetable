package com.timetable.timetable.domain.schedule.service;

import com.timetable.timetable.domain.schedule.repository.SubjectRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;
}
