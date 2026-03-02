## Dominio


### entidades
- Cohort: Turma de estudantes de um curso. Usada para separa-los de modo a caberem nas salas.
- Course: Entidade que ancora as disciplinas. Governada pelo coordenador da mesma.
- Room: Recurso para hospedar aulas. Tem capacidade predefinida.
- Subject: disciplina a ser lecionada por n docentes e n horas(definido pelos creditos)
- TimeSlot(lesson assignment): aula planeada para uma disciplina a ser lecionada por docente x as horas y na sala z ao cohort A
- Timetable: entidade que agrega as aulas a serem lecionadas em um periodo academico(ex: 2026 ou 2026 1 semestre)

32 horas
18 + 8
16 horas por semana obrigatorio tempo inteiro
parciais 8h semanais

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

## testing
There are unit test for all services avaliable, to run them *cd* into api and then run:

``` bash
mvn test -Dtest="*ServiceTest"
```
**make sure you have maven installed for it**

