package com.timetable.timetable.domain.schedule.repository;

import com.timetable.timetable.domain.schedule.dto.RoomResponse;
import com.timetable.timetable.domain.schedule.entity.Room;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByName(String name);
    Page<Room> findAll(Pageable pageable);
}
