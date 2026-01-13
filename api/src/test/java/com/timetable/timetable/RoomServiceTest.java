package com.timetable.timetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.timetable.timetable.domain.schedule.dto.CreateRoomRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateRoomRequest;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.exception.RoomNotFoundException;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;
import com.timetable.timetable.domain.schedule.service.RoomService;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    
    @InjectMocks
    private RoomService roomService;
    
    private Room testRoom;
    
    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
            .id(1L)
            .name("Room 101")
            .capacity(30)
            .build();
    }
    
    @Test
    void createRoom_Success() {
        // Given
        CreateRoomRequest request = new CreateRoomRequest("Room 102", 40);
        
        when(roomRepository.existsByName("Room 102")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Room result = roomService.createRoom(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Room 102");
        assertThat(result.getCapacity()).isEqualTo(40);
        verify(roomRepository, times(1)).save(any(Room.class));
    }
    
    @Test
    void createRoom_DuplicateName_ThrowsException() {
        // Given
        CreateRoomRequest request = new CreateRoomRequest("Room 101", 30);
        
        when(roomRepository.existsByName("Room 101")).thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> roomService.createRoom(request))
            .isInstanceOf(IllegalStateException.class);
    }
    
    @Test
    void getAll_ReturnsPageOfRooms() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Room> page = new PageImpl<>(List.of(testRoom), pageable, 1);
        
        when(roomRepository.findAll(pageable)).thenReturn(page);
        
        // When
        Page<Room> result = roomService.getAll(pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testRoom);
    }
    
    @Test
    void getById_RoomExists_ReturnsRoom() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        
        // When
        Room result = roomService.getById(1L);
        
        // Then
        assertThat(result).isEqualTo(testRoom);
    }
    
    @Test
    void getById_RoomNotFound_ThrowsException() {
        // Given
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> roomService.getById(999L))
            .isInstanceOf(RoomNotFoundException.class)
            .hasMessageContaining("Room not found");
    }
    
    @Test
    void updateRoom_Success() {
        // Given
        UpdateRoomRequest request = new UpdateRoomRequest("Room 101 Updated", 50);
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.existsByName("Room 101 Updated")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Room result = roomService.updateRoom(1L, request);
        
        // Then
        assertThat(result.getName()).isEqualTo("Room 101 Updated");
        assertThat(result.getCapacity()).isEqualTo(50);
        verify(roomRepository, times(1)).save(testRoom);
    }
    
    @Test
    void updateRoom_SameName_Success() {
        // Given
        UpdateRoomRequest request = new UpdateRoomRequest("Room 101", 50);
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Room result = roomService.updateRoom(1L, request);
        
        // Then
        assertThat(result.getCapacity()).isEqualTo(50);
        verify(roomRepository, never()).existsByName(anyString());
        verify(roomRepository, times(1)).save(testRoom);
    }
    
    @Test
    void updateRoom_DuplicateName_ThrowsException() {
        // Given
        UpdateRoomRequest request = new UpdateRoomRequest("Existing Room", 40);
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.existsByName("Existing Room")).thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> roomService.updateRoom(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Another room with that name already exists");
    }
    
    @Test
    void deleteRoom_Success() {
        // Given
        when(roomRepository.existsById(1L)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(1L);
        
        // When
        roomService.deleteRoom(1L);
        
        // Then
        verify(roomRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void deleteRoom_RoomNotFound_ThrowsException() {
        // Given
        when(roomRepository.existsById(999L)).thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> roomService.deleteRoom(999L))
            .isInstanceOf(RoomNotFoundException.class)
            .hasMessageContaining("Room not found");
    }
}
