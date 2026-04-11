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

# frontend refactor prompt
You are a senior frontend engineer doing a focused visual design pass on a Vue 3 + Tailwind CSS management system. Your job is to apply a specific set of design improvements consistently across the entire project. Do NOT change any logic, stores, composables, props, emits, or component APIs. Only touch visual/styling code.

## Step 1 — Explore the project structure first

Before making any changes, read and understand the project:
- Find the main layout file (likely src/layouts/ or App.vue) to understand the page shell
- Find the PageHeader component (likely src/components/ui/PageHeader.vue or similar)
- Find the FilterBar component if it exists
- List all page-level views in src/views/ or src/pages/
- Check tailwind.config.js or tailwind.config.ts to understand the current color setup
- Check if there is a global CSS file (src/assets/main.css, src/style.css, or similar)

Report what you find before proceeding.

## Step 2 — Apply these 3 global changes

### A. PageHeader left accent border
In the PageHeader component, find the outermost wrapper div of the header card/panel (the white bg container, not the page root div). Add these classes to it:
  border-l-[3px] border-l-blue-800

If the component uses dynamic classes or a :class binding, append these. The intent is a visible 3px left border in the brand blue on every page header across the app.

### B. Page background
Find where the page/shell background color is set. This is likely on the main layout wrapper, the router-view container, or a global CSS body/html rule. Change the background from bg-white, bg-gray-50, or bg-gray-100 to bg-slate-100. If it is set in CSS rather than Tailwind, change it to #f1f5f9. Apply this only to the page-level shell background, not to cards or panels.

### C. Section and filter labels
Search across ALL .vue files for elements that have ALL of these classes together: uppercase, tracking-wider (or tracking-wide), and text-xs or text-[10px], and are currently colored with text-gray-400 or text-gray-500. These are structural section/filter labels. For every one found:
- Change the color class to text-blue-800
- Add font-bold if not already present
- Change text-xs to text-[10px] if not already that size

Do NOT apply this to badge text, table column headers in data tables, or any label that sits inside a colored background element.

## Step 3 — Apply these per-component changes

### Border radius standardisation
Go through every .vue file and apply this hierarchy:
- Outermost card/panel containers: change rounded-xl or rounded-2xl to rounded-[10px]
- Inner row elements, subject rows, filter inputs, modal inner sections: change rounded-xl or rounded-lg to rounded-md (6px)
- Badge/pill/tag elements (small inline colored spans): change rounded-lg or rounded-xl to rounded (4px)
- Status pills that represent a live state (active, published, valid, etc.): leave as rounded-full
Do not change border radius on buttons — leave those as-is.

### Active row accent bar
In any list where rows are expandable or selectable (course list, user list, cohort list), find the expanded/active row state. Add a left accent element inside the row for the active/expanded state:
  <div class="w-[3px] h-8 rounded-r bg-blue-800 flex-shrink-0"></div>
This should appear as the first child inside the row flex container when the row is expanded or active. For rows that are not expanded/active, render a same-sized transparent div to preserve alignment:
  <div class="w-[3px] h-8 flex-shrink-0"></div>

## Step 4 — Verify consistency

After making all changes, do a final pass:
- Check that no page-level card still has bg-gray-50 or bg-white as its outer page background
- Check that the PageHeader left border appears in every view
- Check that no section label is still text-gray-400 with uppercase tracking
- List every file you changed with a one-line summary of what changed

Make all changes directly to the source files. Do not create new files. Do not refactor component structure. Do not touch script blocks.
