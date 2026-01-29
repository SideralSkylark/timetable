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

    <!-- Loading State -->
    <div v-if="loading" class="max-w-6xl mx-auto text-center py-12">
      <p class="text-gray-500">A carregar cursos...</p>
    </div>

    <!-- Lista de Cursos -->
    <div v-else class="max-w-6xl mx-auto space-y-4">
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
                <span class="text-sm text-gray-600 flex items-center gap-1">
                  <User class="w-3.5 h-3.5" />
                  {{ course.coordinatorName || 'Sem coordenador' }}
                </span>
                <span class="text-sm text-gray-400">•</span>
                <span class="text-sm text-gray-600 flex items-center gap-1">
                  <BookOpen class="w-3.5 h-3.5" />
                  {{ course.subjectCount }} disciplina{{ course.subjectCount !== 1 ? 's' : '' }}
                </span>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <!-- Editar Curso -->
            <button
              @click="openEditCourseModal(course)"
              class="p-2 text-gray-400 hover:text-blue-900 hover:bg-blue-50 rounded transition"
              title="Editar curso"
            >
              <Edit2 class="w-4 h-4" />
            </button>

            <button
              @click="deleteCourse(course.id)"
              class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition"
              title="Eliminar curso"
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

            <div v-if="course.loadingSubjects" class="text-gray-500 text-sm text-center py-4">
              A carregar disciplinas...
            </div>

            <div v-else-if="course.disciplines.length === 0" class="text-gray-400 text-sm text-center py-4">
              Nenhuma disciplina cadastrada
            </div>

            <div v-else class="space-y-2">
              <div
                v-for="subject in course.disciplines"
                :key="subject.id"
                class="bg-white p-4 rounded-lg border border-gray-200 flex items-center justify-between hover:border-blue-200 transition"
              >
                <div class="flex-1">
                  <p class="font-medium text-gray-900">{{ subject.name }}</p>
                  <div class="flex items-center gap-3 mt-1 text-sm text-gray-500">
                    <span>{{ subject.credits }} créditos</span>
                    <span>•</span>
                    <span>{{ subject.targetYear }}º Ano</span>
                    <span>•</span>
                    <span>{{ subject.targetSemester }}º Semestre</span>
                  </div>
                  <div class="mt-2 flex items-center gap-2">
                    <span class="text-sm font-medium text-gray-600">Professores:</span>
                    <div v-if="subject.eligibleTeachers?.length > 0" class="flex flex-wrap gap-1">
                      <span
                        v-for="teacher in subject.eligibleTeachers"
                        :key="teacher.id"
                        class="inline-flex items-center gap-1 px-2 py-0.5 bg-blue-50 text-blue-900 rounded text-xs"
                      >
                        <User class="w-3 h-3" />
                        {{ teacher.username }}
                      </span>
                    </div>
                    <span v-else class="text-sm text-gray-400">Nenhum professor elegível</span>
                  </div>
                </div>

                <div class="flex items-center gap-2">
                  <button
                    @click="openEditDisciplineModal(subject, course)"
                    class="p-2 text-gray-400 hover:text-blue-900 hover:bg-blue-50 rounded transition"
                    title="Editar disciplina"
                  >
                    <Edit2 class="w-4 h-4" />
                  </button>

                  <button
                    @click="deleteDiscipline(subject.id, course)"
                    class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition"
                    title="Eliminar disciplina"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="courses.length === 0" class="text-center py-12 bg-white rounded-lg border border-gray-200">
        <BookOpen class="w-12 h-12 text-gray-300 mx-auto mb-3" />
        <p class="text-gray-500">Nenhum curso cadastrado</p>
        <button
          @click="openCourseModal()"
          class="mt-4 text-blue-900 hover:underline text-sm"
        >
          Criar primeiro curso
        </button>
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
        @cancel="closeCourseModal"
        @submit="createCourse"
      />

      <!-- Editar Curso -->
      <CrudForm
        v-else
        :fields="courseFields"
        :title="`Editar Curso — ${editingCourse.name}`"
        :data="editingCourse"
        @cancel="closeCourseModal"
        @submit="updateCourse"
      />
    </div>

    <!-- Modal Disciplina -->
    <div
      v-if="showDisciplineModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
    >
      <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b border-gray-200">
          <h2 class="text-xl font-bold text-gray-900">
            {{ editingDiscipline ? `Editar Disciplina — ${editingDiscipline.name}` : `Nova Disciplina — Curso: ${selectedCourse?.name}` }}
          </h2>
        </div>

        <div class="p-6 space-y-4">
          <!-- Nome -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nome da Disciplina</label>
            <input
              v-model="disciplineForm.name"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent"
              placeholder="Ex: Cálculo I"
            />
          </div>

          <!-- Créditos -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Número de Créditos</label>
            <input
              v-model.number="disciplineForm.credits"
              type="number"
              min="1"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent"
              placeholder="Ex: 6"
            />
          </div>

          <!-- Ano e Semestre -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Ano</label>
              <input
                v-model.number="disciplineForm.targetYear"
                type="number"
                min="1"
                max="6"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent"
                placeholder="1, 2, 3..."
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Semestre</label>
              <input
                v-model.number="disciplineForm.targetSemester"
                type="number"
                min="1"
                max="2"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent"
                placeholder="1 ou 2"
              />
            </div>
          </div>

          <!-- Seleção de Professores -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Professores Elegíveis</label>
            
            <!-- Campo de busca -->
            <div class="relative mb-3">
              <input
                v-model="teacherSearchQuery"
                type="text"
                class="w-full px-3 py-2 pl-9 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent"
                placeholder="Buscar professores por nome..."
              />
              <Search class="w-4 h-4 text-gray-400 absolute left-3 top-3" />
            </div>

            <!-- Professores selecionados -->
            <div v-if="disciplineForm.selectedTeachers.length > 0" class="mb-3 flex flex-wrap gap-2">
              <div
                v-for="teacher in disciplineForm.selectedTeachers"
                :key="teacher.id"
                class="inline-flex items-center gap-2 px-3 py-1.5 bg-blue-900 text-white rounded-lg text-sm"
              >
                <User class="w-3.5 h-3.5" />
                <span>{{ teacher.username }}</span>
                <button
                  @click="removeTeacher(teacher.id)"
                  class="hover:bg-blue-800 rounded p-0.5"
                >
                  <X class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>

            <!-- Lista de professores disponíveis -->
            <div class="border border-gray-300 rounded-lg max-h-48 overflow-y-auto">
              <div v-if="loadingTeachers" class="p-4 text-center text-gray-500 text-sm">
                A carregar professores...
              </div>
              <div v-else-if="filteredTeachers.length === 0" class="p-4 text-center text-gray-400 text-sm">
                Nenhum professor encontrado
              </div>
              <div v-else>
                <button
                  v-for="teacher in filteredTeachers"
                  :key="teacher.id"
                  @click="toggleTeacher(teacher)"
                  class="w-full px-4 py-2.5 flex items-center justify-between hover:bg-gray-50 border-b border-gray-100 last:border-b-0 transition"
                  :class="{
                    'bg-blue-50': isTeacherSelected(teacher.id)
                  }"
                >
                  <div class="flex items-center gap-3">
                    <div
                      class="w-5 h-5 rounded border-2 flex items-center justify-center"
                      :class="isTeacherSelected(teacher.id) ? 'border-blue-900 bg-blue-900' : 'border-gray-300'"
                    >
                      <Check v-if="isTeacherSelected(teacher.id)" class="w-3 h-3 text-white" />
                    </div>
                    <div class="text-left">
                      <p class="font-medium text-gray-900">{{ teacher.username }}</p>
                      <p class="text-xs text-gray-500">{{ teacher.email }}</p>
                    </div>
                  </div>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="p-6 border-t border-gray-200 flex justify-end gap-3">
          <button
            @click="closeDisciplineModal"
            class="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
          >
            Cancelar
          </button>
          <button
            @click="submitDiscipline"
            class="px-4 py-2 bg-blue-900 text-white rounded-lg hover:bg-blue-800 transition"
            :disabled="!isDisciplineFormValid"
            :class="{ 'opacity-50 cursor-not-allowed': !isDisciplineFormValid }"
          >
            {{ editingDiscipline ? 'Atualizar' : 'Criar' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import CrudForm from '@/component/ui/CrudForm.vue'
import { courseService } from '@/services/courseService'
import { subjectService } from '@/services/subjectService'
import { teacherService } from '@/services/teacherService'
import type { CourseListResponse, CoordinatorOption } from '@/services/dto/course'

import {
  Plus,
  Edit2,
  Trash2,
  ChevronDown,
  ChevronRight,
  BookOpen,
  GraduationCap,
  User,
  X,    
  Check,
  Search
} from 'lucide-vue-next'

interface Teacher {
  id: number
  username: string
  email: string
  enabled: boolean
}

// State
const courses = ref<any[]>([])
const coordinators = ref<CoordinatorOption[]>([])
const teachers = ref<Teacher[]>([])
const loading = ref(false)
const loadingTeachers = ref(false)
const showCourseModal = ref(false)
const showDisciplineModal = ref(false)
const selectedCourse = ref<any>(null)
const editingCourse = ref<any>(null)
const editingDiscipline = ref<any>(null)
const teacherSearchQuery = ref('')

const disciplineForm = ref({
  name: '',
  credits: 0,
  targetYear: 1,
  targetSemester: 1,
  selectedTeachers: [] as Teacher[],
})

// Computed 
const courseFields = computed(() => [
  { name: 'name', type: 'text', placeholder: 'Nome do Curso', required: true },
  {
    name: 'coordinatorId',
    type: 'select',
    placeholder: 'Coordenador',
    required: true,
    options: (coordinators.value || []).map(c => ({
      value: c.id,
      label: c.name
    }))    
  },
])

const filteredTeachers = computed(() => {
  const query = teacherSearchQuery.value.toLowerCase().trim()
  if (!query) return teachers.value
  
  return teachers.value.filter(t => 
    t.username.toLowerCase().includes(query) || 
    t.email.toLowerCase().includes(query)
  )
})

const isDisciplineFormValid = computed(() => {
  return disciplineForm.value.name.trim() !== '' &&
         disciplineForm.value.credits > 0 &&
         disciplineForm.value.targetYear > 0 &&
         disciplineForm.value.targetSemester > 0 &&
         disciplineForm.value.selectedTeachers.length > 0
})


// ========================
// Fetch inicial
// ========================
const loadCourses = async () => {
  loading.value = true
  try {
    const page = await courseService.getAll(0, 50)
    courses.value = page.content.map((c: CourseListResponse) => ({
      ...c,
      disciplines: [],
      expanded: false,
      loadingSubjects: false,
    }))
  } catch (error) {
    console.error('Erro ao carregar cursos:', error)
  } finally {
    loading.value = false
  }
}

const loadCoordinators = async () => {
  try {
    const page = await courseService.getCoordinators()
    coordinators.value = page.content
  } catch (error) {
    console.error('Erro ao carregar coordenadores:', error)
    coordinators.value = []
  }
}

const loadTeachers = async () => {
  loadingTeachers.value = true
  try {
    const page = await teacherService.getAll(0, 1000)
    teachers.value = page.content
  } catch (error) {
    console.error('Erro ao carregar professores:', error)
  } finally {
    loadingTeachers.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadCourses(), loadCoordinators()])
})

// ========================
// Toggle curso + lazy load disciplinas
// ========================
const toggleCourse = async (course: any) => {
  course.expanded = !course.expanded

  // Carrega disciplinas apenas quando expandir pela primeira vez
  if (course.expanded && course.disciplines.length === 0) {
    await loadSubjects(course)
  }
}

const loadSubjects = async (course: any, force = false) => {
  if (course.loadingSubjects) return
  if (!force && course.disciplines.length > 0) return

  course.loadingSubjects = true
  try {
    const page = await subjectService.getAllByCourse(course.id, 0, 100)
    course.disciplines = page.content
  } catch (error) {
    console.error('Erro ao carregar disciplinas:', error)
  } finally {
    course.loadingSubjects = false
  }
}

// ========================
// Cursos CRUD
// ========================
const createCourse = async (data: any) => {
  try {
    await courseService.create({
      name: data.name,
      coordinatorId: Number(data.coordinatorId),
    })
    closeCourseModal()
    await loadCourses()
  } catch (error) {
    console.error('Erro ao criar curso:', error)
  }
}

const updateCourse = async (data: any) => {
  if (!editingCourse.value) return
  try {
    await courseService.update(editingCourse.value.id, {
      name: data.name,
      coordinatorId: Number(data.coordinatorId),
    })
    closeCourseModal()
    await loadCourses()
  } catch (error) {
    console.error('Erro ao atualizar curso:', error)
  }
}

const deleteCourse = async (id: number) => {
  if (!confirm('Tem certeza que deseja eliminar este curso?')) return
  
  try {
    await courseService.delete(id)
    await loadCourses()
  } catch (error) {
    console.error('Erro ao eliminar curso:', error)
  }
}

// ========================
// Disciplinas CRUD
// ========================
const resetDisciplineForm = () => {
  disciplineForm.value = {
    name: '',
    credits: 0,
    targetYear: 1,
    targetSemester: 1,
    selectedTeachers: [],
  }
  teacherSearchQuery.value = ''
}

const submitDiscipline = async () => {
  if (!isDisciplineFormValid.value) return

  const data = {
    name: disciplineForm.value.name,
    credits: disciplineForm.value.credits,
    targetYear: disciplineForm.value.targetYear,
    targetSemester: disciplineForm.value.targetSemester,
    courseId: selectedCourse.value.id,
    eligibleTeacherIds: disciplineForm.value.selectedTeachers.map(t => t.id),
  }

  try {
    if (editingDiscipline.value) {
      await subjectService.update(editingDiscipline.value.id, data)
    } else {
      await subjectService.create(data)
      const courseIndex = courses.value.findIndex(c => c.id === selectedCourse.value.id)
      if (courseIndex !== -1) {
        courses.value[courseIndex].subjectCount++
      }
    }
    
    closeDisciplineModal()
    await loadSubjects(selectedCourse.value, true)
  } catch (error) {
    console.error('Erro ao salvar disciplina:', error)
  }
}

const deleteDiscipline = async (disciplineId: number, course: any) => {
  if (!confirm('Tem certeza que deseja eliminar esta disciplina?')) return
  
  try {
    await subjectService.delete(disciplineId)
    await loadSubjects(course, true)
    
    const courseIndex = courses.value.findIndex(c => c.id === course.id)
    if (courseIndex !== -1) {
      courses.value[courseIndex].subjectCount--
    }
  } catch (error) {
    console.error('Erro ao eliminar disciplina:', error)
  }
}

// ========================
// Teacher selection
// ========================
const isTeacherSelected = (teacherId: number) => {
  return disciplineForm.value.selectedTeachers.some(t => t.id === teacherId)
}

const toggleTeacher = (teacher: Teacher) => {
  const index = disciplineForm.value.selectedTeachers.findIndex(t => t.id === teacher.id)
  if (index > -1) {
    disciplineForm.value.selectedTeachers.splice(index, 1)
  } else {
    disciplineForm.value.selectedTeachers.push(teacher)
  }
}

const removeTeacher = (teacherId: number) => {
  const index = disciplineForm.value.selectedTeachers.findIndex(t => t.id === teacherId)
  if (index > -1) {
    disciplineForm.value.selectedTeachers.splice(index, 1)
  }
}

// ========================
// Modal handlers
// ========================
const openCourseModal = () => {
  editingCourse.value = null
  showCourseModal.value = true
}

const openEditCourseModal = (course: any) => {
  editingCourse.value = { ...course }
  showCourseModal.value = true
}

const closeCourseModal = () => {
  showCourseModal.value = false
  editingCourse.value = null
}

const openDisciplineModal = async (course: any) => {
  selectedCourse.value = course
  editingDiscipline.value = null
  resetDisciplineForm()
  
  if (teachers.value.length === 0) {
    await loadTeachers()
  }
  
  showDisciplineModal.value = true
}

const openEditDisciplineModal = async (discipline: any, course: any) => {
  selectedCourse.value = course
  editingDiscipline.value = { ...discipline }
  
  if (teachers.value.length === 0) {
    await loadTeachers()
  }

  const teachersCopy = (discipline.eligibleTeachers || []).map((t: any) => ({ ...t }))
  
  disciplineForm.value = {
    name: discipline.name,
    credits: discipline.credits,
    targetYear: discipline.targetYear,
    targetSemester: discipline.targetSemester,
    selectedTeachers: teachersCopy,
  }
  
  showDisciplineModal.value = true
}

const closeDisciplineModal = () => {
  showDisciplineModal.value = false
  editingDiscipline.value = null
  selectedCourse.value = null
  resetDisciplineForm()
}
</script
