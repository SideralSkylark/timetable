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

# frontend refactor prompt 1
You are making a focused visual polish pass on a Vue 3 + Tailwind CSS project.
Do NOT change any logic, script blocks, props, emits, or component structure.
Only change Tailwind class strings in template blocks. Make exactly these changes:

## 1. Sidebar.vue — background and border
On the <aside> element:
- Change `bg-white` to `bg-slate-50`
- Change `border-gray-100` to `border-slate-200`

## 2. Sidebar.vue — active nav item
In the RouterLink `:class` binding, find the active state classes.
- Change the active background from `bg-blue-900` to `bg-blue-50`
- Change the active text from `text-white` to `text-blue-800`
- Keep `font-semibold` on the active state
On the active nav item icon <component :is>, change `text-white` (active) to `text-blue-800`
On the ChevronRight icon that appears on active items, change `text-white/60` to `text-blue-400`

## 3. Sidebar.vue — brand subtitle
Find the <p> element with the "Sistema de gestão" text.
- Replace its current color and size classes with:
  `text-[10px] text-blue-800 font-bold uppercase tracking-wider`

## 4. PageHeader.vue — icon box
Find the div wrapping the icon with `bg-blue-900`.
- Change `bg-blue-900` to `bg-blue-50`
- On the <component :is="icon"> inside it, add `text-blue-800` class
  (or change the existing color class to `text-blue-800`)
- Change `p-2.5` to `p-2` to compensate for the lighter background
  (the darker bg made it feel smaller than it is)

## 5. CrudTable.vue — column header labels
Find all <th> elements in the <thead>.
- Change `text-xs font-medium text-gray-400 uppercase tracking-wide`
  to `text-[10px] font-bold text-blue-800 uppercase tracking-wider`
Do NOT change the empty <th> at the end (the actions column header).

## 6. CrudTable.vue — edit button hover color
Find the edit button with `hover:text-blue-900 hover:border-blue-200 hover:bg-blue-50`.
- Change to `hover:text-amber-600 hover:border-amber-200 hover:bg-amber-50`

## 7. FilterBar.vue — active filter count badge
Find the <span> element that displays `activeFilterCount` with classes
`bg-blue-900 text-white`.
- Change `bg-blue-900 text-white` to `bg-blue-100 text-blue-800`

After all changes, verify:
- No other classes were changed beyond the 7 items listed
- Script blocks are untouched
- No new elements were added or removed
- Report which line in each file was changed


# frontend refactor prompt 2
You are making a second focused polish pass on a Vue 3 + Tailwind CSS project.
Do NOT change any logic, script blocks, props, emits, reactive state, or computed properties.
Only change icon imports, icon references in templates, and Tailwind class strings.
Make exactly these changes in order:

## ICONS

### 1. Sidebar.vue — standardise room icon
In the dashboardRoutes array in <script>, find the Rooms entry using the `Building` icon.
- Change it to use `DoorOpen` instead.
- Update the import at the top: remove `Building` from lucide imports, add `DoorOpen` if not already present.

### 2. Sidebar.vue — standardise courses icon
In the dashboardRoutes array, find the Courses entry using the `School` icon.
- Change it to `GraduationCap`. GraduationCap is already imported (as CohortIcon alias) — add a clean import for it or reuse the existing one.
- Remove the `School` import from lucide imports.

### 3. Sidebar.vue — fix duplicate CalendarDays icons
In the dashboardRoutes array, find the MyTimetableView entry (Meu Horário) which uses `CalendarDays`.
- Change it to use `CalendarCheck` instead.
- Add `CalendarCheck` to the lucide imports.
- Leave the Timetable entry (Horários) using `CalendarDays` unchanged.

### 4. Sidebar.vue — fix Cohorts icon
In the dashboardRoutes array, find the Cohorts entry (currently using `CohortIcon` which is `GraduationCap`).
- Change it to use `BookOpen`.
- Add `BookOpen` to the lucide imports if not present.
- Remove the `CohortIcon` alias and its source `GraduationCap as CohortIcon` import line.

### 5. Rooms.vue (or wherever the Rooms PageHeader is) — standardise room icon
Find the PageHeader `:icon` prop for the rooms page. If it uses `Building`, change it to `DoorOpen`.
Update the import accordingly.

## MODALS

### 6. ResetPasswordModal.vue — icon container shape
Find the div wrapping the `KeyRound` icon with classes `bg-amber-50 p-3 rounded-full`.
- Change `rounded-full` to `rounded-md`
- Change `p-3` to `p-2`

### 7. All modal footer buttons — radius consistency
In every .vue file, find cancel and confirm/submit button pairs that appear inside modal <div> containers (fixed inset-0 overlays). These buttons currently use `rounded-lg`.
- Change all cancel and confirm buttons inside modals from `rounded-lg` to `rounded-md`.
- Do NOT change the primary action buttons in PageHeader #actions slots — those stay as `rounded-lg`.
- Do NOT change buttons in FilterBar — those stay as-is.
The rule: if a button is inside a `fixed inset-0` modal overlay, it gets `rounded-md`. Everything else keeps its current radius.

### 8. Rooms.vue — fix English button text
In the rooms modal submit button, find the template strings:
- Change `'Updating...'` to `'A guardar...'`
- Change `'Creating...'` to `'A criar...'`
- Change `'Update Room'` to `'Guardar alterações'`
- Change `'Create Room'` to `'Criar sala'`

## DEPTH / SHADOW

### 9. PageHeader.vue — remove shadow from chrome element
On the main container div of PageHeader (the one with `shadow-sm border border-gray-100`):
- Remove `shadow-sm`
- Change `border-gray-100` to `border-slate-200`

### 10. FilterBar.vue — remove shadow from chrome element
On the main container div of FilterBar (the one with `shadow-sm border border-gray-100`):
- Remove `shadow-sm`
- Change `border-gray-100` to `border-slate-200`

### 11. Cohorts.vue — table header labels
Find all <th> elements in the cohorts table thead that have `text-xs font-medium text-gray-400 uppercase tracking-wide`.
- Change to `text-[10px] font-bold text-blue-800 uppercase tracking-wider`

### 12. GenericForm.vue — field label style
Find the <label> element inside the `v-for="field in fields"` loop.
- Change its text classes from `text-xs font-medium text-gray-500` to `text-[10px] font-bold text-blue-800 uppercase tracking-wider`
- Remove the `mb-1.5` if present and replace with `mb-2` to compensate for the smaller font size.

## VERIFICATION
After all changes:
- Confirm no script block was modified
- Confirm no component structure changed
- List every file touched and the specific line(s) changed in each
- Flag any case where an icon was used in a place you couldn't find (so it can be checked manually)
