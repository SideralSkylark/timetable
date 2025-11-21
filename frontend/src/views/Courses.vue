<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- Header -->
    <div class="max-w-6xl mx-auto mb-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="bg-blue-900 p-3 rounded-lg">
              <GraduationCap class="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">Gestão de Cursos</h1>
              <p class="text-gray-500 text-sm">Gerir cursos e suas disciplinas</p>
            </div>
          </div>

          <button
            @click="openCourseModal()"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition"
          >
            <Plus class="w-5 h-5" />
            Novo Curso
          </button>
        </div>
      </div>
    </div>

    <!-- Lista de Cursos -->
    <div class="max-w-6xl mx-auto space-y-4">
      <div
        v-for="course in courses"
        :key="course.id"
        class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden"
      >
        <!-- Course Header -->
        <div class="p-5 flex items-center justify-between hover:bg-gray-50 transition">
          <div class="flex items-center gap-4 flex-1">
            <button
              @click="toggleCourse(course)"
              class="text-gray-600 hover:text-blue-900 transition"
            >
              <ChevronDown v-if="course.expanded" class="w-5 h-5" />
              <ChevronRight v-else class="w-5 h-5" />
            </button>

            <div class="bg-blue-900 bg-opacity-10 p-2 rounded">
              <BookOpen class="w-5 h-5 text-white" />
            </div>

            <div class="flex-1">
              <h3 class="font-semibold text-gray-900">{{ course.name }}</h3>
              <div class="flex items-center gap-4 mt-1">
                <span class="text-sm text-gray-500">Coordenador ID: {{ course.coordinatorId }}</span>
                <span class="text-sm text-gray-500">•</span>
                <span class="text-sm text-gray-500">{{ course.disciplines?.length ?? 0 }} disciplinas</span>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <!-- Editar Curso -->
            <button
              @click="openEditCourseModal(course)"
              class="p-2 text-gray-400 hover:text-blue-900 hover:bg-blue-50 rounded transition"
            >
              <Edit2 class="w-4 h-4" />
            </button>

            <button
              @click="deleteCourse(course.id)"
              class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition"
            >
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- Disciplinas -->
        <div v-if="course.expanded" class="border-t border-gray-200 bg-gray-50">
          <div class="p-5">
            <div class="flex items-center justify-between mb-4">
              <h4 class="font-medium text-gray-700">Disciplinas</h4>

              <button
                @click="openDisciplineModal(course)"
                class="text-blue-900 text-sm flex items-center gap-1 hover:underline"
              >
                <Plus class="w-4 h-4" /> Adicionar Disciplina
              </button>
            </div>

            <div v-if="course.loadingSubjects" class="text-gray-500 text-sm">
              A carregar disciplinas...
            </div>

            <div v-else class="space-y-2">
              <div
                v-for="subject in course.disciplines"
                :key="subject.id"
                class="bg-white p-4 rounded-lg border border-gray-200 flex items-center justify-between"
              >
                <div>
                  <p class="font-medium text-gray-900">{{ subject.name }}</p>
                  <p class="text-sm text-gray-500">
                    Professores: {{ subject.teachers.map((t: TeacherInfo) => t.name).join(', ') }}
                  </p>
                </div>

                <div class="flex items-center gap-2">
                  <!-- EDIT FUTURO DISCIPLINA -->
                  <button
                    @click="openEditDisciplineModal(subject)"
                    class="p-2 text-gray-400 hover:text-blue-900 hover:bg-blue-50 rounded transition"
                  >
                    <Edit2 class="w-4 h-4" />
                  </button>

                  <button
                    @click="deleteDiscipline(subject.id)"
                    class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Curso -->
<div
  v-if="showCourseModal"
  class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
>
  <!-- Criar Curso -->
  <CrudForm
    v-if="!editingCourse"
    :fields="courseFields"
    title="Novo Curso"
    is-create
    @cancel="() => { showCourseModal = false }"
    @submit="createCourse"
  />

  <!-- Editar Curso -->
  <CrudForm
    v-else
    :fields="courseFields"
    :title="`Editar Curso — ${editingCourse.name}`"
    :data="editingCourse"
    @cancel="() => { showCourseModal = false; editingCourse = null }"
    @submit="updateCourse"
  />
</div>

  <!-- Modal Disciplina -->
<div
  v-if="showDisciplineModal"
  class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
>
  <!-- Criar Disciplina -->
  <CrudForm
    v-if="!editingDiscipline"
    :fields="disciplineFields"
    :title="`Nova Disciplina — Curso: ${selectedCourse?.name}`"
    is-create
    @cancel="() => { showDisciplineModal = false }"
    @submit="createDiscipline"
  />

  <!-- Editar Disciplina -->
  <CrudForm
    v-else
    :fields="disciplineFields"
    :title="`Editar Disciplina — Curso: ${selectedCourse?.name}`"
    :data="editingDiscipline"
    @cancel="() => { showDisciplineModal = false; editingDiscipline = null }"
    @submit="updateDiscipline"
  />
</div>
</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import CrudForm from '@/component/ui/CrudForm.vue'
import { courseService } from '@/services/courseService'
import { subjectService } from '@/services/subjectService'
import type { TeacherInfo } from '@/services/dto/subject'
import type { UpdateCourseRequest } from '@/services/dto/course'

import {
  Plus,
  Edit2,
  Trash2,
  ChevronDown,
  ChevronRight,
  BookOpen,
  GraduationCap,
} from 'lucide-vue-next'

const courses = ref<any[]>([])
const showCourseModal = ref(false)
const showDisciplineModal = ref(false)
const selectedCourse = ref<any>(null)
const editingCourse = ref<any>(null)
const editingDiscipline = ref<any>(null)

const courseFields = [
  { name: 'name', type: 'text', placeholder: 'Nome do Curso' },
  { name: 'coordinatorId', type: 'number', placeholder: 'ID do Coordenador' },
]

const disciplineFields = [
  { name: 'name', type: 'text', placeholder: 'Nome da Disciplina' },
  { name: 'teacherIds', type: 'text', placeholder: 'IDs dos Professores (separados por ,)' },
]

// ========================
// Fetch inicial
// ========================
const loadCourses = async () => {
  const page = await courseService.getAll(0, 50)
  courses.value = page.content.map(c => ({
    ...c,
    disciplines: [],
    expanded: false,
    loadingSubjects: false,
  }))
}

onMounted(loadCourses)

// ========================
// Toggle curso + lazy load disciplinas
// ========================
const toggleCourse = async (course: any) => {
  course.expanded = !course.expanded

  if (course.expanded && course.disciplines.length === 0) {
    course.loadingSubjects = true
    const page = await subjectService.getAll(0, 100)
    course.disciplines = page.content.filter(s => s.courseId === course.id)
    course.loadingSubjects = false
  }
}

// ========================
// Criar curso
// ========================
const createCourse = async (data: any) => {
  await courseService.create({
    name: data.name,
    coordinatorId: Number(data.coordinatorId),
  })
  showCourseModal.value = false
  loadCourses()
}

// ========================
// Atualizar curso
// ========================
const updateCourse = async (data: any) => {
  if (!editingCourse.value) return
  await courseService.update(editingCourse.value.id, {
    name: data.name,
    coordinatorId: Number(data.coordinatorId),
  })
  showCourseModal.value = false
  editingCourse.value = null
  loadCourses()
}

// ========================
// Criar disciplina
// ========================
const createDiscipline = async (data: any) => {
  await subjectService.create({
    name: data.name,
    courseId: selectedCourse.value.id,
    teacherIds: data.teacherIds.split(',').map((x: string) => Number(x.trim())),
  })
  showDisciplineModal.value = false
  toggleCourse(selectedCourse.value) // reload subjects
}

const updateDiscipline = async (data: any) => {
  if (!editingDiscipline.value) return
  await subjectService.update(editingDiscipline.value.id, {
    name: data.name,
    teacherIds: data.teacherIds.split(',').map((x: string) => Number(x.trim())),
  })
  showDisciplineModal.value = false
  editingDiscipline.value = null
  toggleCourse(selectedCourse.value) // reload subjects
}

const openEditDisciplineModal = (discipline: any) => {
  selectedCourse.value = courses.value.find(c =>
    c.disciplines.some(d => d.id === discipline.id)
  )
  editingDiscipline.value = { ...discipline }
  showDisciplineModal.value = true
}

// ========================
// Delete curso
// ========================
const deleteCourse = async (id: number) => {
  await courseService.delete(id)
  loadCourses()
}

// ========================
// Delete disciplina
// ========================
const deleteDiscipline = async (disciplineId: number) => {
  await subjectService.delete(disciplineId)
  loadCourses()
}

// ========================
// Abrir modals
// ========================
const openCourseModal = () => {
  editingCourse.value = null
  showCourseModal.value = true
}

const openEditCourseModal = (course: UpdateCourseRequest) => {
  editingCourse.value = { ...course }
  showCourseModal.value = true
}

const openDisciplineModal = (course: any) => {
  selectedCourse.value = course
  editingDiscipline.value = null
  showDisciplineModal.value = true
}
</script>
