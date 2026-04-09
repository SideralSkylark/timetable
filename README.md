# University Timetable Management System

A comprehensive automated solution for university timetable scheduling and academic management. The system utilizes constraint satisfaction algorithms to optimize resource allocation across courses, cohorts, and faculty.

## Technology Stack

### Backend
- Java 21
- Spring Boot 3.x
- Timefold Solver (Constraint Satisfaction Engine)
- Spring Security with JWT
- PostgreSQL
- Maven

### Frontend
- Vue.js 3 (Composition API)
- Vite
- Pinia (State Management)
- Tailwind CSS 4
- TypeScript
- Lucide Vue Next (Iconography)

## Project Structure

### Backend (api/)
The backend follows a modular Spring Boot architecture:
- `auth/`: Authentication logic, session management, and JWT issuance.
- `common/`: Global exception handling, response wrappers, and shared utilities.
- `config/`: System configuration including Security, CORS, and data initializers.
- `domain/`: JPA entities representing the core business model (Users, Courses, Rooms, Subjects).
- `scheduler_engine/`: The core optimization engine.
    - `domain/`: Timefold planning entities and solutions.
    - `solver/`: Constraint definitions and score calculation logic.
    - `preparation/`: Data transformation logic to prepare datasets for the solver.
- `security/`: JWT filters and security context configuration.

### Frontend (frontend/)
A modern SPA built with Vue.js 3:
- `component/`: Reusable UI components (Tables, Forms, Pagination).
- `composables/`: Shared reactive logic (e.g., toast notifications).
- `layouts/`: Application structural templates.
- `services/`: API integration layer and Data Transfer Objects (DTOs).
- `stores/`: Centralized state management using Pinia.
- `views/`: Page-level components and specific business logic.

## Domain Model

- **Cohort**: A specific group of students within a course, used for room capacity management.
- **Course**: The primary academic entity that anchors subjects and is managed by a Coordinator.
- **Room**: Physical resource with predefined capacity.
- **Subject**: Academic discipline with specific credit hours and faculty requirements.
- **Lesson Assignment**: A scheduled instance of a subject assigned to a teacher, timeslot, and room.
- **Timetable**: Aggregation of lesson assignments for a specific academic period.

## Scheduler Engine

The generation process follows three distinct phases:
1. **Data Preparation**: Validation of cohorts and greedy teacher assignment based on workload balancing.
2. **Lesson Initialization**: Creation of lesson assignments based on subject credits.
3. **Constraint Optimization**: The Timefold solver assigns optimal timeslots and rooms while respecting hard constraints (e.g., teacher/room conflicts) and soft constraints.

## Testing

Unit tests are provided for core services. To execute tests:

```bash
cd api
mvn test -Dtest="*ServiceTest"
```

---

## N+1 Query Inventory

The following N+1 query patterns have been identified in the backend and require optimization (e.g., using `JOIN FETCH` or `@EntityGraph`):

1. **`CohortSubjectRepository.findByAcademicYearAndSemesterAndIsActive`**
   - **Usage**: `PreSolverService` and `TeacherAssignmentService`.
   - **Problem**: Iterating over the results to calculate teacher workloads or generate display names triggers separate queries for `Cohort`, `Subject`, and `ApplicationUser` (assignedTeacher) for *each* `CohortSubject`.
   - **Impact**: High. This occurs during the critical data preparation phase of the scheduler engine.

2. **`SubjectRepository.search` (and other finders like `findByTargetYearAndTargetSemester`)**
   - **Usage**: `SubjectController` (list views).
   - **Problem**: Returns a list of `Subject` without fetching the associated `Course` or the `eligibleTeachers` collection.
   - **Impact**: Medium. Causes multiple queries when displaying a list of subjects with their course names or eligible teacher counts.

3. **`CohortRepository` list finders (e.g., `findByCourseId`, `findByAcademicYear`)**
   - **Usage**: `CohortController`.
   - **Problem**: Fetches `Cohort` entities without joining the `Course` or the `students` collection.
   - **Impact**: Medium. Accessing `getStudentCount()` (accesses `students.size()`) or `getDisplayName()` (accesses `course.getName()`) triggers an N+1 for each cohort in the list.

4. **`ScheduledClassRepository.findByCohortId` (and `findByTeacherId`, `findByCohortSubjectId`)**
   - **Usage**: `ScheduledClassController`.
   - **Problem**: These methods use simple JPQL that does not `JOIN FETCH` related entities like `cohortSubject`, `room`, `timeslot`, or `timetable`.
   - **Impact**: Medium. Displays of scheduled classes in the UI will trigger separate queries for each row's details.

5. **`UserRepository.findAllByRole` (and Specification-based `findAll`)**
   - **Usage**: `UserService.getAllUsers`.
   - **Problem**: `ApplicationUser.roles` is marked as `FetchType.EAGER`, but the queries often do not use `FETCH JOIN`.
   - **Impact**: Low/Medium. Depending on Hibernate's execution plan, it may fetch roles in separate queries for each user in the result list.

---
