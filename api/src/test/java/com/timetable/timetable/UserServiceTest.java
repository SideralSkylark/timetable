package com.timetable.timetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.timetable.timetable.auth.exception.UserAlreadyExistsException;
import com.timetable.timetable.domain.user.dto.CreateUser;
import com.timetable.timetable.domain.user.entity.*;
import com.timetable.timetable.domain.user.exception.UserNotFoundException;
import com.timetable.timetable.domain.user.repository.UserRepository;
import com.timetable.timetable.domain.user.repository.UserRoleRepository;
import com.timetable.timetable.domain.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserRoleRepository roleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private ApplicationUser testUser;
    private UserRoleEntity adminRole;
    private UserRoleEntity teacherRole;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        UserRoleEntity adminRole = new UserRoleEntity();
        adminRole.setId(1L);
        adminRole.setRole(UserRole.ADMIN);
            
        UserRoleEntity teacherRole = new UserRoleEntity();
        teacherRole.setId(2L);
        teacherRole.setRole(UserRole.TEACHER);
            
        testUser = ApplicationUser.builder()
            .id(1L)
            .username("john.doe")
            .email("john.doe@school.edu")
            .password("encodedPassword")
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .roles(Set.of(adminRole))
            .build();
    }
    
    @Test
    void createUser_Success() {
        // Given
        CreateUser request = new CreateUser(
            "jane.smith", 
            "jane.smith@school.edu", 
            "password123", 
            List.of("ADMIN", "TEACHER")
        );
        
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByRole(UserRole.ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByRole(UserRole.TEACHER)).thenReturn(Optional.of(teacherRole));
        when(userRepository.save(any(ApplicationUser.class))).thenReturn(testUser);
        
        // When
        ApplicationUser result = userService.createUser(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john.doe");
        verify(userRepository, times(1)).save(any(ApplicationUser.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }
    
    @Test
    void createUser_UsernameAlreadyExists_ThrowsException() {
        // Given
        CreateUser request = new CreateUser(
            "existing.user", 
            "new@school.edu", 
            "password123", 
            List.of("TEACHER")
        );
        
        when(userRepository.existsByUsername("existing.user")).thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("Username is already taken");
    }
    
    @Test
    void getUserById_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When
        ApplicationUser result = userService.getUserById(1L);
        
        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    void getUserById_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User 999 not found");
    }
    
    @Test
    void getAllUsers_ReturnsAllUsers() {
        // Given
        List<ApplicationUser> users = Arrays.asList(
            testUser,
            ApplicationUser.builder()
                .id(2L)
                .username("jane.doe")
                .email("jane@school.edu")
                .password("pass")
                .build()
        );
        when(userRepository.findAll()).thenReturn(users);
        
        // When
        List<ApplicationUser> result = userService.getAllUsers();
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("john.doe");
    }
    
    @Test
    void getAllUsers_Pageable_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<ApplicationUser> users = List.of(testUser);
        Page<ApplicationUser> page = new PageImpl<>(users, pageable, 1);
        
        when(userRepository.findAll(pageable)).thenReturn(page);
        
        // When
        Page<ApplicationUser> result = userService.getAllUsers(pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testUser);
    }
    
    @Test
    void deleteById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);
        
        // When
        userService.deleteById(1L);
        
        // Then
        verify(userRepository, times(1)).delete(testUser);
    }
    
    @Test
    void deleteById_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userService.deleteById(999L))
            .isInstanceOf(UserNotFoundException.class);
    }
}
