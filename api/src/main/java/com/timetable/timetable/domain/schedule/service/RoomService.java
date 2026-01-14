package com.timetable.timetable.domain.schedule.service;

import com.timetable.timetable.domain.schedule.dto.CreateRoomRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateRoomRequest;
import com.timetable.timetable.domain.schedule.entity.Course;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.exception.RoomNotFoundException;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final CourseService courseService;
    
    public Room createRoom(CreateRoomRequest roomRequest) {
        log.debug("Creating room");

        if (roomRepository.existsByName(roomRequest.name())) {
            log.error("There is already a room with the same name: {}", roomRequest.name());
            throw new IllegalStateException();
        } 

        Course course = courseService.getById(roomRequest.restrictedToCourseId());

        Room room = Room.builder()
            .name(roomRequest.name())
            .capacity(roomRequest.capacity())
            .restrictedToCourse(course)
            .build();

        Room saved = roomRepository.save(room);

        log.info("Room {} created", saved.getId());
        return saved;
    }

    public Page<Room> getAll(Pageable pageable) {
        return roomRepository.findAll(pageable);
    }

    public Room getById(Long id) {
        log.debug("Looking for room: {}", id);
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RoomNotFoundException("Room not found."));

        log.info("Room {} found", id);
        return room;
    }

    public Room updateRoom(Long id, UpdateRoomRequest updateRequest) {
        log.debug("Updating room {}", id);
        Room room = getById(id);

        if (!room.getName().equals(updateRequest.name()) && roomRepository.existsByName(updateRequest.name())) {
            log.warn("Another room with the same name already exists");
            throw new IllegalArgumentException("Another room with that name already exists.");
        }

        Course course = courseService.getById(updateRequest.restrictedToCourseId());

        room.setName(updateRequest.name());
        room.setCapacity(updateRequest.capacity());
        room.setRestrictedToCourse(course);

        Room saved = roomRepository.save(room);

        log.info("Updated room {}", id);
        return saved;
    }

    public void deleteRoom(Long id) {
        log.debug("Deleting room {}", id);
        if (!roomRepository.existsById(id)) {
            throw new RoomNotFoundException("Room not found.");
        }

        roomRepository.deleteById(id);
        log.info("Room {} deleted", id);
    }
}
