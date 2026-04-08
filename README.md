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



TODO:



1. High Priority - Reliability and Data Integrity

- Refactor solver persistence: Currently, the timetable solution is only saved if the frontend polls for it. It should be persisted immediately after the solver job completes via a solver listener or job lifecycle callback to prevent data loss if the browser session ends.

- Implement persistent job tracking: Move the in-memory Job map to a database-backed system to allow job recovery and status tracking across server restarts.

- Centralize asynchronous resource management: Replace manual ExecutorService instances with Spring's @Async abstraction and a managed ThreadPoolTaskExecutor to prevent unmanaged resource exhaustion.

- Password reset logic: Implement admin-triggered password reset that generates a temporary random password for manual delivery to users.



2. Medium Priority - Performance and Scalability

- Resolve N+1 query patterns: Refactor CohortService and other entity managers to use bulk fetching (findAllById) instead of iterative lookups when assigning related entities like students or subjects.

- Externalize business constraints: Move hardcoded logic from the TimetableConstraintProvider (e.g., year-period rules) to the database or configuration files to improve flexibility.

- Enhance generation polling: Enable the frontend to detect ongoing generation jobs on mount to prevent losing track of solver progress during page refreshes.



3. Low Priority - Maintainability and Code Quality

- Refactor monolithic frontend components: Break down Timetable.vue and other large views into atomic, reusable components (e.g., Grid, SidePanel, Modals) to improve readability.

- Standardize data mapping: Adopt MapStruct or a similar tool to replace manual builder-based mapping in services, reducing boilerplate and potential errors.

- Improve TypeScript type safety: Eliminate the use of "any" in Pinia stores (specifically in course and timetable stores) by defining comprehensive interfaces for all API payloads.



Considerations:

the modal to add students to cohort fetches 100 pages (if possible find a more reasonable sollution)
