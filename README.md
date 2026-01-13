## Dominio

### entidades
- Cohort: Turma de estudantes de um curso. Usada para separa-los de modo a caberem nas salas.
- Course: Entidade que ancora as disciplinas. Governada pelo coordenador da mesma.
- Room: Recurso para hospedar aulas. Tem capacidade predefinida.
- Subject: disciplina a ser lecionada por n docentes e n horas(definido pelos creditos)
- TimeSlot(lesson assignment): aula planeada para uma disciplina a ser lecionada por docente x as horas y na sala z ao cohort A
- Timetable: entidade que agrega as aulas a serem lecionadas em um periodo academico(ex: 2026 ou 2026 1 semestre)


## Solver
### entidades
- TfRoom: recurso a ser usado pela planning sollution


## testing
There are unit test for all services avaliable, to run them *cd* into api and then run:

``` bash
mvn test -Dtest="*ServiceTest"
```
**make sure you have maven installed for it**

