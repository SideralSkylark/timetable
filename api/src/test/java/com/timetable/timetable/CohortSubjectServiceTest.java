package com.timetable.timetable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.timetable.timetable.domain.schedule.dto.CreateCohortSubjectRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateCohortSubjectRequest;
import com.timetable.timetable.domain.schedule.entity.Cohort;
import com.timetable.timetable.domain.schedule.entity.CohortSubject;
import com.timetable.timetable.domain.schedule.entity.Course;
import com.timetable.timetable.domain.schedule.entity.Subject;
import com.timetable.timetable.domain.schedule.exception.CohortSubjectNotFoundException;
import com.timetable.timetable.domain.schedule.repository.CohortSubjectRepository;
import com.timetable.timetable.domain.schedule.service.CohortService;
import com.timetable.timetable.domain.schedule.service.CohortSubjectService;
import com.timetable.timetable.domain.schedule.service.SubjectService;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.entity.UserRoleEntity;
import com.timetable.timetable.domain.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CohortSubjectService Tests")
class CohortSubjectServiceTest {

    @Mock
    private CohortSubjectRepository cohortSubjectRepository;

    @Mock
    private CohortService cohortService;

    @Mock
    private SubjectService subjectService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CohortSubjectService cohortSubjectService;

    private Course course;
    private Cohort cohort;
    private Subject subject;
    private ApplicationUser teacher;
    private ApplicationUser nonTeacher;
    private UserRoleEntity teacherRole;
    private UserRoleEntity studentRole;

    @BeforeEach
    void setUp() {
        course = Course.builder()
            .id(1L)
            .name("Computer Science")
            .build();

        teacherRole = new UserRoleEntity(1L, UserRole.TEACHER);
        studentRole = new UserRoleEntity(2L, UserRole.STUDENT);

        teacher = ApplicationUser.builder()
            .id(1L)
            .username("teacher1")
            .roles(Set.of(teacherRole))
            .build();

        nonTeacher = ApplicationUser.builder()
            .id(2L)
            .username("student1")
            .roles(Set.of(studentRole))
            .build();

        cohort = Cohort.builder()
            .id(1L)
            .year(1)
            .section("A")
            .semester(1)
            .academicYear(2024)
            .course(course)
            .build();

        // Use HashSet mutável para permitir modificações nos testes
        Set<ApplicationUser> eligibleTeachers = new HashSet<>();
        eligibleTeachers.add(teacher);

        subject = Subject.builder()
            .id(1L)
            .name("Programming I")
            .targetYear(1)
            .targetSemester(1)
            .course(course)
            .eligibleTeachers(eligibleTeachers)
            .build();
    }

    @Test
    @DisplayName("Should create cohort subject successfully")
    void shouldCreateCohortSubjectSuccessfully() {
        CreateCohortSubjectRequest request = new CreateCohortSubjectRequest(
            1L, 1L, 1L, 4, 2
        );

        when(cohortService.getById(1L)).thenReturn(cohort);
        when(subjectService.getById(1L)).thenReturn(subject);
        when(userService.getUserById(1L)).thenReturn(teacher);
        when(cohortSubjectRepository.existsByCohortAndSubjectAndAcademicYearAndSemester(
            any(), any(), anyInt(), anyInt()
        )).thenReturn(false);
        when(cohortSubjectRepository.sumWeeklyHoursByTeacher(teacher)).thenReturn(0);
        when(cohortSubjectRepository.save(any(CohortSubject.class))).thenAnswer(inv -> {
            CohortSubject cs = inv.getArgument(0);
            cs.setId(1L);
            return cs;
        });

        CohortSubject result = cohortSubjectService.createCohortSubject(request);

        assertThat(result).isNotNull();
        assertThat(result.getCohort()).isEqualTo(cohort);
        assertThat(result.getSubject()).isEqualTo(subject);
        assertThat(result.getAssignedTeacher()).isEqualTo(teacher);
        assertThat(result.getWeeklyHours()).isEqualTo(4);
        assertThat(result.getLessonsPerWeek()).isEqualTo(2);
        
        verify(cohortSubjectRepository).save(any(CohortSubject.class));
    }

    @Test
    @DisplayName("Should throw exception when teacher is not eligible")
    void shouldThrowExceptionWhenTeacherNotEligible() {
        UserRoleEntity teacherRoleEntity = new UserRoleEntity(3L, UserRole.TEACHER);
        ApplicationUser ineligibleTeacher = ApplicationUser.builder()
            .id(3L)
            .username("teacher2")
            .roles(Set.of(teacherRoleEntity))
            .build();

        CreateCohortSubjectRequest request = new CreateCohortSubjectRequest(
            1L, 1L, 3L, 4, 2
        );

        when(cohortService.getById(1L)).thenReturn(cohort);
        when(subjectService.getById(1L)).thenReturn(subject);
        when(userService.getUserById(3L)).thenReturn(ineligibleTeacher);

        assertThatThrownBy(() -> cohortSubjectService.createCohortSubject(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not eligible");

        verify(cohortSubjectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user is not a teacher")
    void shouldThrowExceptionWhenUserIsNotTeacher() {
        CreateCohortSubjectRequest request = new CreateCohortSubjectRequest(
            1L, 1L, 2L, 4, 2
        );

        when(cohortService.getById(1L)).thenReturn(cohort);
        when(subjectService.getById(1L)).thenReturn(subject);
        when(userService.getUserById(2L)).thenReturn(nonTeacher);

        assertThatThrownBy(() -> cohortSubjectService.createCohortSubject(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a teacher");
    }

    @Test
    @DisplayName("Should throw exception when cohort year doesn't match subject target year")
    void shouldThrowExceptionWhenYearMismatch() {
        Cohort mismatchedCohort = Cohort.builder()
            .id(1L)
            .year(2)
            .semester(1)
            .academicYear(2024)
            .course(course)
            .build();

        CreateCohortSubjectRequest request = new CreateCohortSubjectRequest(
            1L, 1L, 1L, 4, 2
        );

        when(cohortService.getById(1L)).thenReturn(mismatchedCohort);
        when(subjectService.getById(1L)).thenReturn(subject);
        when(userService.getUserById(1L)).thenReturn(teacher);

        assertThatThrownBy(() -> cohortSubjectService.createCohortSubject(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not match cohort year");
    }

    @Test
    @DisplayName("Should throw exception when cohort semester doesn't match subject target semester")
    void shouldThrowExceptionWhenSemesterMismatch() {
        Cohort mismatchedCohort = Cohort.builder()
            .id(1L)
            .year(1)
            .semester(2)
            .academicYear(2024)
            .course(course)
            .build();

        CreateCohortSubjectRequest request = new CreateCohortSubjectRequest(
            1L, 1L, 1L, 4, 2
        );

        when(cohortService.getById(1L)).thenReturn(mismatchedCohort);
        when(subjectService.getById(1L)).thenReturn(subject);
        when(userService.getUserById(1L)).thenReturn(teacher);

        assertThatThrownBy(() -> cohortSubjectService.createCohortSubject(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not match cohort semester");
    }

    @Test
    @DisplayName("Should throw exception when subject already assigned to cohort")
    void shouldThrowExceptionWhenSubjectAlreadyAssigned() {
        CreateCohortSubjectRequest request = new CreateCohortSubjectRequest(
            1L, 1L, 1L, 4, 2
        );

        when(cohortService.getById(1L)).thenReturn(cohort);
        when(subjectService.getById(1L)).thenReturn(subject);
        when(userService.getUserById(1L)).thenReturn(teacher);
        when(cohortSubjectRepository.existsByCohortAndSubjectAndAcademicYearAndSemester(
            cohort, subject, 2024, 1
        )).thenReturn(true);

        assertThatThrownBy(() -> cohortSubjectService.createCohortSubject(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already assigned");

        verify(cohortSubjectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when teacher workload exceeds limit")
    void shouldThrowExceptionWhenWorkloadExceeds() {
        CreateCohortSubjectRequest request = new CreateCohortSubjectRequest(
            1L, 1L, 1L, 5, 2
        );

        when(cohortService.getById(1L)).thenReturn(cohort);
        when(subjectService.getById(1L)).thenReturn(subject);
        when(userService.getUserById(1L)).thenReturn(teacher);
        when(cohortSubjectRepository.existsByCohortAndSubjectAndAcademicYearAndSemester(
            any(), any(), anyInt(), anyInt()
        )).thenReturn(false);
        when(cohortSubjectRepository.sumWeeklyHoursByTeacher(teacher)).thenReturn(4);

        assertThatThrownBy(() -> cohortSubjectService.createCohortSubject(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exceed maximum weekly hours");
    }

    @Test
    @DisplayName("Should get all cohort subjects paginated")
    void shouldGetAllCohortSubjectsPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        List<CohortSubject> cohortSubjects = Arrays.asList(
            CohortSubject.builder().id(1L).build(),
            CohortSubject.builder().id(2L).build()
        );
        Page<CohortSubject> page = new PageImpl<>(cohortSubjects, pageable, cohortSubjects.size());

        when(cohortSubjectRepository.findAll(pageable)).thenReturn(page);

        Page<CohortSubject> result = cohortSubjectService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(cohortSubjectRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get cohort subjects by cohort")
    void shouldGetCohortSubjectsByCohort() {
        Pageable pageable = PageRequest.of(0, 10);
        List<CohortSubject> cohortSubjects = Arrays.asList(
            CohortSubject.builder().id(1L).cohort(cohort).build()
        );
        Page<CohortSubject> page = new PageImpl<>(cohortSubjects, pageable, cohortSubjects.size());

        when(cohortService.getById(1L)).thenReturn(cohort);
        when(cohortSubjectRepository.findByCohort(cohort, pageable)).thenReturn(page);

        Page<CohortSubject> result = cohortSubjectService.getByCohort(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(cohortSubjectRepository).findByCohort(cohort, pageable);
    }

    @Test
    @DisplayName("Should get cohort subjects by subject")
    void shouldGetCohortSubjectsBySubject() {
        Pageable pageable = PageRequest.of(0, 10);
        List<CohortSubject> cohortSubjects = Arrays.asList(
            CohortSubject.builder().id(1L).subject(subject).build()
        );
        Page<CohortSubject> page = new PageImpl<>(cohortSubjects, pageable, cohortSubjects.size());

        when(subjectService.getById(1L)).thenReturn(subject);
        when(cohortSubjectRepository.findBySubject(subject, pageable)).thenReturn(page);

        Page<CohortSubject> result = cohortSubjectService.getBySubject(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(cohortSubjectRepository).findBySubject(subject, pageable);
    }

    @Test
    @DisplayName("Should get cohort subjects by teacher")
    void shouldGetCohortSubjectsByTeacher() {
        Pageable pageable = PageRequest.of(0, 10);
        List<CohortSubject> cohortSubjects = Arrays.asList(
            CohortSubject.builder().id(1L).assignedTeacher(teacher).build()
        );
        Page<CohortSubject> page = new PageImpl<>(cohortSubjects, pageable, cohortSubjects.size());

        when(userService.getUserById(1L)).thenReturn(teacher);
        when(cohortSubjectRepository.findByAssignedTeacher(teacher, pageable)).thenReturn(page);

        Page<CohortSubject> result = cohortSubjectService.getByTeacher(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(cohortSubjectRepository).findByAssignedTeacher(teacher, pageable);
    }

    @Test
    @DisplayName("Should throw exception when getting by non-teacher user")
    void shouldThrowExceptionWhenGettingByNonTeacher() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.getUserById(2L)).thenReturn(nonTeacher);

        assertThatThrownBy(() -> cohortSubjectService.getByTeacher(2L, pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a teacher");
    }

    @Test
    @DisplayName("Should get cohort subject by id")
    void shouldGetCohortSubjectById() {
        CohortSubject cohortSubject = CohortSubject.builder()
            .id(1L)
            .cohort(cohort)
            .subject(subject)
            .build();

        when(cohortSubjectRepository.findByIdWithDetails(1L))
            .thenReturn(Optional.of(cohortSubject));

        CohortSubject result = cohortSubjectService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(cohortSubjectRepository).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("Should throw exception when cohort subject not found")
    void shouldThrowExceptionWhenCohortSubjectNotFound() {
        when(cohortSubjectRepository.findByIdWithDetails(1L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cohortSubjectService.getById(1L))
            .isInstanceOf(CohortSubjectNotFoundException.class)
            .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should update cohort subject successfully")
    void shouldUpdateCohortSubjectSuccessfully() {
        UpdateCohortSubjectRequest request = new UpdateCohortSubjectRequest(
            1L, 5, 3, true
        );

        CohortSubject existingCohortSubject = CohortSubject.builder()
            .id(1L)
            .cohort(cohort)
            .subject(subject)
            .assignedTeacher(teacher)
            .academicYear(2024)
            .semester(1)
            .weeklyHours(4)
            .lessonsPerWeek(2)
            .isActive(true)
            .build();

        when(cohortSubjectRepository.findByIdWithDetails(1L))
            .thenReturn(Optional.of(existingCohortSubject));
        when(cohortSubjectRepository.sumWeeklyHoursByTeacher(teacher)).thenReturn(0);
        when(cohortSubjectRepository.save(any(CohortSubject.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        CohortSubject result = cohortSubjectService.updateCohortSubject(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getWeeklyHours()).isEqualTo(5);
        assertThat(result.getLessonsPerWeek()).isEqualTo(3);
        verify(cohortSubjectRepository).save(existingCohortSubject);
    }

    @Test
    @DisplayName("Should update cohort subject with new teacher")
    void shouldUpdateCohortSubjectWithNewTeacher() {
        UserRoleEntity newTeacherRole = new UserRoleEntity(4L, UserRole.TEACHER);
        ApplicationUser newTeacher = ApplicationUser.builder()
            .id(4L)
            .username("teacher3")
            .roles(Set.of(newTeacherRole))
            .build();

        // Adicionar o novo professor aos elegíveis
        subject.getEligibleTeachers().add(newTeacher);

        UpdateCohortSubjectRequest request = new UpdateCohortSubjectRequest(
            4L, 5, 3, true
        );

        CohortSubject existingCohortSubject = CohortSubject.builder()
            .id(1L)
            .cohort(cohort)
            .subject(subject)
            .assignedTeacher(teacher)
            .academicYear(2024)
            .semester(1)
            .weeklyHours(4)
            .lessonsPerWeek(2)
            .isActive(true)
            .build();

        when(cohortSubjectRepository.findByIdWithDetails(1L))
            .thenReturn(Optional.of(existingCohortSubject));
        when(userService.getUserById(4L)).thenReturn(newTeacher);
        when(cohortSubjectRepository.sumWeeklyHoursByTeacher(newTeacher)).thenReturn(0);
        when(cohortSubjectRepository.save(any(CohortSubject.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        CohortSubject result = cohortSubjectService.updateCohortSubject(1L, request);

        assertThat(result.getAssignedTeacher()).isEqualTo(newTeacher);
        verify(userService).getUserById(4L);
    }

    @Test
    @DisplayName("Should delete cohort subject successfully")
    void shouldDeleteCohortSubjectSuccessfully() {
        when(cohortSubjectRepository.existsById(1L)).thenReturn(true);
        when(cohortSubjectRepository.countScheduledClassesByCohortSubjectId(1L)).thenReturn(0);
        doNothing().when(cohortSubjectRepository).deleteById(1L);

        cohortSubjectService.deleteCohortSubject(1L);

        verify(cohortSubjectRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent cohort subject")
    void shouldThrowExceptionWhenDeletingNonExistent() {
        when(cohortSubjectRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> cohortSubjectService.deleteCohortSubject(1L))
            .isInstanceOf(CohortSubjectNotFoundException.class)
            .hasMessageContaining("not found");

        verify(cohortSubjectRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when deleting cohort subject with scheduled classes")
    void shouldThrowExceptionWhenDeletingWithScheduledClasses() {
        when(cohortSubjectRepository.existsById(1L)).thenReturn(true);
        when(cohortSubjectRepository.countScheduledClassesByCohortSubjectId(1L)).thenReturn(5);

        assertThatThrownBy(() -> cohortSubjectService.deleteCohortSubject(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("scheduled classes");

        verify(cohortSubjectRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should get subjects for cohort schedule")
    void shouldGetSubjectsForCohortSchedule() {
        List<CohortSubject> activeCohortSubjects = Arrays.asList(
            CohortSubject.builder().id(1L).isActive(true).build(),
            CohortSubject.builder().id(2L).isActive(true).build()
        );

        when(cohortService.getById(1L)).thenReturn(cohort);
        when(cohortSubjectRepository.findActiveByCohort(cohort))
            .thenReturn(activeCohortSubjects);

        List<CohortSubject> result = cohortSubjectService.getSubjectsForCohortSchedule(1L);

        assertThat(result).hasSize(2);
        verify(cohortSubjectRepository).findActiveByCohort(cohort);
    }

    @Test
    @DisplayName("Should get cohort subjects by academic year and semester")
    void shouldGetCohortSubjectsByAcademicYearAndSemester() {
        List<CohortSubject> cohortSubjects = Arrays.asList(
            CohortSubject.builder().id(1L).academicYear(2024).semester(1).build(),
            CohortSubject.builder().id(2L).academicYear(2024).semester(1).build()
        );

        when(cohortSubjectRepository.findByAcademicYearAndSemester(2024, 1))
            .thenReturn(cohortSubjects);

        List<CohortSubject> result = cohortSubjectService
            .getByAcademicYearAndSemester(2024, 1);

        assertThat(result).hasSize(2);
        verify(cohortSubjectRepository).findByAcademicYearAndSemester(2024, 1);
    }

    @Test
    @DisplayName("Should get total weekly hours for cohort")
    void shouldGetTotalWeeklyHoursForCohort() {
        when(cohortService.getById(1L)).thenReturn(cohort);
        when(cohortSubjectRepository.sumWeeklyHoursByCohort(cohort)).thenReturn(20);

        int result = cohortSubjectService.getTotalWeeklyHoursForCohort(1L);

        assertThat(result).isEqualTo(20);
        verify(cohortSubjectRepository).sumWeeklyHoursByCohort(cohort);
    }
}
