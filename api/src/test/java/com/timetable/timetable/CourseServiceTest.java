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

import com.timetable.timetable.domain.schedule.dto.CreateCourseRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateCourseRequest;
import com.timetable.timetable.domain.schedule.entity.Course;
import com.timetable.timetable.domain.schedule.exception.CourseNotFoundException;
import com.timetable.timetable.domain.schedule.repository.CourseRepository;
import com.timetable.timetable.domain.schedule.service.CourseService;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.entity.UserRoleEntity;
import com.timetable.timetable.domain.user.service.UserService;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private CourseService courseService;
    
    private ApplicationUser testCoordinator;
    private ApplicationUser testNonCoordinator;
    private Course testCourse;
    
    @BeforeEach
    void setUp() {
        UserRoleEntity coordinatorRole = new UserRoleEntity();
        coordinatorRole.setId(1L);
        coordinatorRole.setRole(UserRole.COORDINATOR);
        
        UserRoleEntity teacherRole = new UserRoleEntity();
        teacherRole.setId(2L);
        teacherRole.setRole(UserRole.TEACHER);
        
        testCoordinator = ApplicationUser.builder()
            .id(1L)
            .username("coordinator")
            .email("coordinator@school.edu")
            .build();
        testCoordinator.getRoles().add(coordinatorRole);
        
        testNonCoordinator = ApplicationUser.builder()
            .id(2L)
            .username("teacher")
            .email("teacher@school.edu")
            .build();
        testNonCoordinator.getRoles().add(teacherRole);
        
        testCourse = Course.builder()
            .id(1L)
            .name("Computer Science")
            .coordinator(testCoordinator)
            .build();
    }
    
    @Test
    void createCourse_Success() {
        // Given
        CreateCourseRequest request = new CreateCourseRequest("Engineering", 1L);
        
        when(courseRepository.existsByName("Engineering")).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(testCoordinator);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Course result = courseService.createCourse(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Engineering");
        assertThat(result.getCoordinator()).isEqualTo(testCoordinator);
        verify(courseRepository, times(1)).save(any(Course.class));
    }
    
    @Test
    void createCourse_DuplicateName_ThrowsException() {
        // Given
        CreateCourseRequest request = new CreateCourseRequest("Computer Science", 1L);
        
        when(courseRepository.existsByName("Computer Science")).thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> courseService.createCourse(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Course already exists");
    }
    
    @Test
    void createCourse_NonCoordinator_ThrowsException() {
        // Given
        CreateCourseRequest request = new CreateCourseRequest("Engineering", 2L);
        
        when(courseRepository.existsByName("Engineering")).thenReturn(false);
        when(userService.getUserById(2L)).thenReturn(testNonCoordinator);
        
        // When/Then
        assertThatThrownBy(() -> courseService.createCourse(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a coordinator");
    }
    
    @Test
    void getAll_ReturnsPageOfCourses() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(List.of(testCourse), pageable, 1);
        
        when(courseRepository.findAll(pageable)).thenReturn(page);
        
        // When
        Page<Course> result = courseService.getAll(pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testCourse);
    }
    
    @Test
    void getById_CourseExists_ReturnsCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        
        // When
        Course result = courseService.getById(1L);
        
        // Then
        assertThat(result).isEqualTo(testCourse);
    }
    
    @Test
    void getById_CourseNotFound_ThrowsException() {
        // Given
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> courseService.getById(999L))
            .isInstanceOf(CourseNotFoundException.class)
            .hasMessageContaining("No course with id: 999");
    }
    
    @Test
    void updateCourse_Success() {
        // Given
        UpdateCourseRequest request = new UpdateCourseRequest("Updated Name", 1L);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByName("Updated Name")).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(testCoordinator);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Course result = courseService.updateCourse(1L, request);
        
        // Then
        assertThat(result.getName()).isEqualTo("Updated Name");
        verify(courseRepository, times(1)).save(testCourse);
    }
    
    @Test
    void updateCourse_DuplicateName_ThrowsException() {
        // Given
        UpdateCourseRequest request = new UpdateCourseRequest("Existing Course", 1L);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByName("Existing Course")).thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> courseService.updateCourse(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Another course with that name already exists");
    }
    
    @Test
    void updateCourse_NonCoordinator_ThrowsException() {
        // Given
        UpdateCourseRequest request = new UpdateCourseRequest("Updated Name", 2L);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByName("Updated Name")).thenReturn(false);
        when(userService.getUserById(2L)).thenReturn(testNonCoordinator);
        
        // When/Then
        assertThatThrownBy(() -> courseService.updateCourse(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a coordinator");
    }
    
    @Test
    void deleteCourse_Success() {
        // Given
        when(courseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(courseRepository).deleteById(1L);
        
        // When
        courseService.deleteCourse(1L);
        
        // Then
        verify(courseRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void deleteCourse_CourseNotFound_ThrowsException() {
        // Given
        when(courseRepository.existsById(999L)).thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> courseService.deleteCourse(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No course with id: 999");
    }
}
