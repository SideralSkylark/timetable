package com.timetable.timetable.domain.schedule.service;

import com.timetable.timetable.domain.schedule.dto.CreateRoomRequest;
import com.timetable.timetable.domain.schedule.dto.RoomResponse;
import com.timetable.timetable.domain.schedule.dto.UpdateRoomRequest;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.exception.RoomNotFoundException;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    
    public RoomResponse createRoom(CreateRoomRequest roomRequest) {
        if (roomRepository.existsByName(roomRequest.name())) {
            throw new IllegalStateException();
        } 

        Room room = Room.builder()
            .name(roomRequest.name())
            .capacity(roomRequest.capacity())
            .build();

        Room saved = roomRepository.save(room);
        
        return RoomResponse.from(saved);
    }

    public Page<RoomResponse> getAll(Pageable pageable) {
        return roomRepository.findAll(pageable)
            .map(RoomResponse::from);
    }

    public RoomResponse getById(Long id) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RoomNotFoundException("Room not found."));

        return RoomResponse.from(room);
    }

    public Room getRoomById(Long id) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RoomNotFoundException("Room not found."));

        return room;
    }

    public RoomResponse updateRoom(Long id, UpdateRoomRequest updateRequest) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RoomNotFoundException("Room not found."));

        if (!room.getName().equals(updateRequest.name()) && roomRepository.existsByName(updateRequest.name())) {
            throw new IllegalArgumentException("Another room with that name already exists.");
        }

        room.setName(updateRequest.name());
        room.setCapacity(updateRequest.capacity());

        Room saved = roomRepository.save(room);

        return RoomResponse.from(saved);
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RoomNotFoundException("Room not found.");
        }

        roomRepository.deleteById(id);
    }
}
