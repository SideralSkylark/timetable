// package com.timetable.timetable;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;
//
// import java.util.*;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
//
// import com.timetable.timetable.domain.schedule.dto.CreateCohortRequest;
// import com.timetable.timetable.domain.schedule.dto.UpdateCohortRequest;
// import com.timetable.timetable.domain.schedule.entity.Cohort;
// import com.timetable.timetable.domain.schedule.entity.Course;
// import com.timetable.timetable.domain.schedule.exception.CohortNotFoundException;
// import com.timetable.timetable.domain.schedule.repository.CohortRepository;
// import com.timetable.timetable.domain.schedule.service.CohortService;
// import com.timetable.timetable.domain.schedule.service.CourseService;
// import com.timetable.timetable.domain.user.entity.ApplicationUser;
// import com.timetable.timetable.domain.user.entity.UserRole;
// import com.timetable.timetable.domain.user.entity.UserRoleEntity;
// import com.timetable.timetable.domain.user.service.UserService;
//
// @ExtendWith(MockitoExtension.class)
// class CohortServiceTest {
//
//     @Mock
//     private CohortRepository cohortRepository;
//
//     @Mock
//     private CourseService courseService;
//
//     @Mock
//     private UserService userService;
//
//     @InjectMocks
//     private CohortService cohortService;
//
//     private Course testCourse;
//     private ApplicationUser testStudent;
//     private ApplicationUser testTeacher;
//     private Cohort testCohort;
//
//     @BeforeEach
//     void setUp() {
//         testCourse = Course.builder()
//             .id(1L)
//             .name("Computer Science")
//             .build();
//
//         UserRoleEntity studentRole = new UserRoleEntity();
//         studentRole.setId(1L);
//         studentRole.setRole(UserRole.STUDENT);
//
//
//         UserRoleEntity teacherRole = new UserRoleEntity();
//         teacherRole.setId(1L);
//         teacherRole.setRole(UserRole.TEACHER);
//
//         testStudent = ApplicationUser.builder()
//             .id(101L)
//             .username("student1")
//             .email("student1@school.edu")
//             .build();
//         testStudent.getRoles().add(studentRole);
//
//         testTeacher = ApplicationUser.builder()
//             .id(102L)
//             .username("teacher1")
//             .email("teacher1@school.edu")
//             .build();
//         testTeacher.getRoles().add(teacherRole);
//
//         testCohort = Cohort.builder()
//             .id(1L)
//             .year(1)
//             .section("A")
//             .academicYear(2025)
//             .course(testCourse)
//             .students(Set.of(testStudent))
//             .build();
//     }
//
//     @Test
//     void createCohort_Success() {
//         // Given
//         CreateCohortRequest request = new CreateCohortRequest(
//             1, "A", 2025, 1L, List.of(101L)
//         );
//
//         when(cohortRepository.existsByYearAndSectionAndAcademicYearAndCourseId(anyInt(), anyString(), anyInt(), anyLong()))
//             .thenReturn(false);
//         when(courseService.getById(1L)).thenReturn(testCourse);
//         when(userService.getUserById(101L)).thenReturn(testStudent);
//         when(cohortRepository.save(any(Cohort.class))).thenReturn(testCohort);
//
//         // When
//         Cohort result = cohortService.createCohort(request);
//
//         // Then
//         assertThat(result).isNotNull();
//         assertThat(result.getId()).isEqualTo(1L);
//         assertThat(result.getStudents()).contains(testStudent);
//         verify(cohortRepository, times(1)).save(any(Cohort.class));
//     }
//
//     @Test
//     void createCohort_DuplicateCohort_ThrowsException() {
//         // Given
//         CreateCohortRequest request = new CreateCohortRequest(
//             1, "A", 2025, 1L, List.of()
//         );
//
//         when(cohortRepository.existsByYearAndSectionAndAcademicYearAndCourseId(1, "A", 2025, 1L))
//             .thenReturn(true);
//
//         // When/Then
//         assertThatThrownBy(() -> cohortService.createCohort(request))
//             .isInstanceOf(IllegalStateException.class)
//             .hasMessageContaining("already exists");
//     }
//
//     @Test
//     void createCohort_NonStudentUser_ThrowsException() {
//         // Given
//         CreateCohortRequest request = new CreateCohortRequest(
//             1, "A", 2025, 1L, List.of(102L)
//         );
//
//         when(cohortRepository.existsByYearAndSectionAndAcademicYearAndCourseId(anyInt(), anyString(), anyInt(), anyLong()))
//             .thenReturn(false);
//         when(courseService.getById(1L)).thenReturn(testCourse);
//         when(userService.getUserById(102L)).thenReturn(testTeacher);
//
//         // When/Then
//         assertThatThrownBy(() -> cohortService.createCohort(request))
//             .isInstanceOf(IllegalArgumentException.class)
//             .hasMessageContaining("is not a student");
//     }
//
//     @Test
//     void getById_CohortExists_ReturnsCohort() {
//         // Given
//         when(cohortRepository.findById(1L)).thenReturn(Optional.of(testCohort));
//
//         // When
//         Cohort result = cohortService.getById(1L);
//
//         // Then
//         assertThat(result).isEqualTo(testCohort);
//     }
//
//     @Test
//     void getById_CohortNotFound_ThrowsException() {
//         // Given
//         when(cohortRepository.findById(999L)).thenReturn(Optional.empty());
//
//         // When/Then
//         assertThatThrownBy(() -> cohortService.getById(999L))
//             .isInstanceOf(CohortNotFoundException.class);
//     }
//
//     @Test
//     void getAll_ReturnsPageOfCohorts() {
//         // Given
//         Pageable pageable = PageRequest.of(0, 10);
//         Page<Cohort> page = new PageImpl<>(List.of(testCohort), pageable, 1);
//
//         when(cohortRepository.findAll(pageable)).thenReturn(page);
//
//         // When
//         Page<Cohort> result = cohortService.getAll(pageable);
//
//         // Then
//         assertThat(result.getContent()).hasSize(1);
//         assertThat(result.getContent().get(0)).isEqualTo(testCohort);
//     }
//
//     @Test
//     void updateCohort_Success() {
//         // Given
//         UpdateCohortRequest request = new UpdateCohortRequest(
//             1, "B", 2025, List.of(101L)
//         );
//
//         when(cohortRepository.findById(1L)).thenReturn(Optional.of(testCohort));
//         when(cohortRepository.existsAnotherWithSameAttributes(anyInt(), anyString(), anyInt(), anyLong(), anyLong()))
//             .thenReturn(false);
//         when(userService.getUserById(101L)).thenReturn(testStudent);
//         when(cohortRepository.save(any(Cohort.class))).thenReturn(testCohort);
//
//         // When
//         Cohort result = cohortService.updateCohort(1L, request);
//
//         // Then
//         assertThat(result).isNotNull();
//         verify(cohortRepository, times(1)).save(testCohort);
//     }
//
//     @Test
//     void deleteCohort_Success() {
//         // Given
//         when(cohortRepository.existsById(1L)).thenReturn(true);
//         doNothing().when(cohortRepository).deleteById(1L);
//
//         // When
//         cohortService.deleteCohort(1L);
//
//         // Then
//         verify(cohortRepository, times(1)).deleteById(1L);
//     }
//
//     @Test
//     void deleteCohort_NotFound_ThrowsException() {
//         // Given
//         when(cohortRepository.existsById(999L)).thenReturn(false);
//
//         // When/Then
//         assertThatThrownBy(() -> cohortService.deleteCohort(999L))
//             .isInstanceOf(CohortNotFoundException.class);
//     }
// }
