package com.timetable.timetable.domain.schedule.service;

import java.util.List;

import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    public List<Room> getAll() {
        return roomRepository.findAll();
    }
}
