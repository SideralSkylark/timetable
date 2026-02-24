## Dominio


### entidades
- Cohort: Turma de estudantes de um curso. Usada para separa-los de modo a caberem nas salas.
- Course: Entidade que ancora as disciplinas. Governada pelo coordenador da mesma.
- Room: Recurso para hospedar aulas. Tem capacidade predefinida.
- Subject: disciplina a ser lecionada por n docentes e n horas(definido pelos creditos)
- TimeSlot(lesson assignment): aula planeada para uma disciplina a ser lecionada por docente x as horas y na sala z ao cohort A
- Timetable: entidade que agrega as aulas a serem lecionadas em um periodo academico(ex: 2026 ou 2026 1 semestre)

estudo autonomo como e calculado? (funcao da carga horaria)
atribuir horarios mesmo sem corpo de docente suficiente (docentes fantasma)


1 dia de estudo autonomo
9 horas sexta feira missa.
surpluss de turmas antes de gerar.


## Solver
### entidades
- TfRoom: recurso a ser usado pela planning sollution

1. PREPARAÇÃO DE DADOS
   ├─ Criar/Validar Cohorts
   ├─ ATRIBUIR PROFESSORES (algoritmo ganancioso)
   │  ├─ Calcular carga de trabalho
   │  ├─ Escolher professor com menor carga
   │  └─ Criar fantasma se todos extrapolam 8h
   └─ Criar CohortSubjects (teacher JÁ definido)

2. GERAR LESSON ASSIGNMENTS
   └─ Para cada CohortSubject:
       └─ Criar N LessonAssignments (N = blocks/week)
           ├─ teacher = cohortSubject.teacher (FIXO)
           ├─ timeslot = null (SOLVER VAI DECIDIR)
           └─ room = null (SOLVER VAI DECIDIR)

3. SOLVER
   └─ Atribui timeslot + room
       └─ Respeita constraint: teacher não pode ter 2 aulas simultâneas

spring-api  | 17:59:05.554 [INFO ] [http-nio-0.0.0.0-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring DispatcherServlet 'dispatcherServlet'
spring-api  | 17:59:05.554 [INFO ] [http-nio-0.0.0.0-8080-exec-1] o.s.web.servlet.DispatcherServlet - Initializing Servlet 'dispatcherServlet'
spring-api  | 17:59:05.557 [INFO ] [http-nio-0.0.0.0-8080-exec-1] o.s.web.servlet.DispatcherServlet - Completed initialization in 3 ms
spring-api  | 17:59:05.797 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 17:59:14.013 [INFO ] [http-nio-0.0.0.0-8080-exec-3] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 17:59:14.199 [INFO ] [http-nio-0.0.0.0-8080-exec-3] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 17:59:14.217 [INFO ] [http-nio-0.0.0.0-8080-exec-3] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 17:59:14.311 [INFO ] [http-nio-0.0.0.0-8080-exec-3] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 17:59:14.314 [INFO ] [http-nio-0.0.0.0-8080-exec-3] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 402
spring-api  | 17:59:14.416 [INFO ] [http-nio-0.0.0.0-8080-exec-3] c.t.t.s.solver.PermutationService - ScheduledClass 402 → 3/14 valid permutations
spring-api  | 17:59:37.669 [INFO ] [http-nio-0.0.0.0-8080-exec-5] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 17:59:37.701 [INFO ] [http-nio-0.0.0.0-8080-exec-5] c.t.t.s.solver.PermutationService - Full swap: ScheduledClass 402 ↔ ScheduledClass 405
spring-api  | 17:59:37.818 [INFO ] [http-nio-0.0.0.0-8080-exec-6] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 17:59:39.921 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 17:59:39.988 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 17:59:39.995 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 17:59:39.998 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 17:59:39.999 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 398
spring-api  | 17:59:40.051 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.solver.PermutationService - ScheduledClass 398 → 1/14 valid permutations
spring-api  | 17:59:42.312 [INFO ] [http-nio-0.0.0.0-8080-exec-9] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 17:59:42.374 [INFO ] [http-nio-0.0.0.0-8080-exec-9] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 17:59:42.379 [INFO ] [http-nio-0.0.0.0-8080-exec-9] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 17:59:42.383 [INFO ] [http-nio-0.0.0.0-8080-exec-9] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 17:59:42.384 [INFO ] [http-nio-0.0.0.0-8080-exec-9] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 400
spring-api  | 17:59:42.426 [INFO ] [http-nio-0.0.0.0-8080-exec-9] c.t.t.s.solver.PermutationService - ScheduledClass 400 → 2/14 valid permutations
spring-api  | 17:59:44.687 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 17:59:44.697 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.solver.PermutationService - Full swap: ScheduledClass 400 ↔ ScheduledClass 405
spring-api  | 17:59:44.774 [INFO ] [http-nio-0.0.0.0-8080-exec-2] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 17:59:57.717 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 17:59:57.765 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 17:59:57.771 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 17:59:57.774 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 17:59:57.775 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 402
spring-api  | 17:59:57.799 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.solver.PermutationService - ScheduledClass 402 → 3/14 valid permutations
spring-api  | 18:00:00.900 [INFO ] [http-nio-0.0.0.0-8080-exec-6] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:00.910 [INFO ] [http-nio-0.0.0.0-8080-exec-6] c.t.t.s.solver.PermutationService - Full swap: ScheduledClass 402 ↔ ScheduledClass 403
spring-api  | 18:00:00.972 [INFO ] [http-nio-0.0.0.0-8080-exec-7] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:02.635 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:02.681 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 18:00:02.686 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 18:00:02.689 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 18:00:02.690 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 399
spring-api  | 18:00:02.712 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.solver.PermutationService - ScheduledClass 399 → 1/14 valid permutations
spring-api  | 18:00:06.332 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:06.380 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 18:00:06.385 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 18:00:06.388 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 18:00:06.388 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 396
spring-api  | 18:00:06.412 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.solver.PermutationService - ScheduledClass 396 → 0/14 valid permutations
spring-api  | 18:00:08.520 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:08.559 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 18:00:08.563 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 18:00:08.565 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 18:00:08.565 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 394
spring-api  | 18:00:08.583 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.solver.PermutationService - ScheduledClass 394 → 0/14 valid permutations
spring-api  | 18:00:10.105 [INFO ] [http-nio-0.0.0.0-8080-exec-2] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:10.147 [INFO ] [http-nio-0.0.0.0-8080-exec-2] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 18:00:10.152 [INFO ] [http-nio-0.0.0.0-8080-exec-2] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 18:00:10.155 [INFO ] [http-nio-0.0.0.0-8080-exec-2] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 18:00:10.155 [INFO ] [http-nio-0.0.0.0-8080-exec-2] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 393
spring-api  | 18:00:10.176 [INFO ] [http-nio-0.0.0.0-8080-exec-2] c.t.t.s.solver.PermutationService - ScheduledClass 393 → 0/14 valid permutations
spring-api  | 18:00:12.283 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:12.320 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 18:00:12.323 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 18:00:12.325 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 18:00:12.326 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 398
spring-api  | 18:00:12.351 [INFO ] [http-nio-0.0.0.0-8080-exec-4] c.t.t.s.solver.PermutationService - ScheduledClass 398 → 3/14 valid permutations
spring-api  | 18:00:14.014 [INFO ] [http-nio-0.0.0.0-8080-exec-6] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:14.022 [INFO ] [http-nio-0.0.0.0-8080-exec-6] c.t.t.s.solver.PermutationService - Full swap: ScheduledClass 398 ↔ ScheduledClass 403
spring-api  | 18:00:14.084 [INFO ] [http-nio-0.0.0.0-8080-exec-7] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:16.584 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:16.620 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 18:00:16.624 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 18:00:16.626 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 18:00:16.626 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 400
spring-api  | 18:00:16.644 [INFO ] [http-nio-0.0.0.0-8080-exec-8] c.t.t.s.solver.PermutationService - ScheduledClass 400 → 2/14 valid permutations
spring-api  | 18:00:23.681 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:23.718 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 18:00:23.723 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 18:00:23.726 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 18:00:23.726 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 395
spring-api  | 18:00:23.749 [INFO ] [http-nio-0.0.0.0-8080-exec-10] c.t.t.s.solver.PermutationService - ScheduledClass 395 → 0/14 valid permutations
spring-api  | 18:00:25.634 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.JwtAuthenticationFilter - Authenticated user 'admin' via cookie
spring-api  | 18:00:25.666 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.m.TimetableSolutionMapper - Rebuilding TimetableSolution from 98 persisted ScheduledClasses
spring-api  | 18:00:25.669 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.m.TimetableSolutionMapper - Rebuilt 98 LessonAssignments
spring-api  | 18:00:25.671 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.solver.PermutationService - [DIAG] Initial score of rebuilt solution: 0hard/0soft
spring-api  | 18:00:25.671 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.solver.PermutationService - Evaluating 14 candidate slots for ScheduledClass 396
spring-api  | 18:00:25.703 [INFO ] [http-nio-0.0.0.0-8080-exec-1] c.t.t.s.solver.PermutationService - ScheduledClass 396 → 0/14 valid permutations

## testing
There are unit test for all services avaliable, to run them *cd* into api and then run:

``` bash
mvn test -Dtest="*ServiceTest"
```
**make sure you have maven installed for it**

