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
          <button @click="openCreateModal"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition">
            <Plus class="w-5 h-5" />
            Novo Curso
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="max-w-6xl mx-auto text-center py-12">
      <p class="text-gray-500">A carregar cursos...</p>
    </div>

    <!-- Course list -->
    <div v-else class="max-w-6xl mx-auto space-y-4">
      <div v-for="course in courses" :key="course.id"
        class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <!-- Course row -->
        <div class="p-5 flex items-center justify-between hover:bg-gray-50 transition">
          <div class="flex items-center gap-4 flex-1">
            <button @click="toggleCourse(course)" class="text-gray-400 hover:text-blue-900 transition">
              <ChevronDown v-if="course.expanded" class="w-5 h-5" />
              <ChevronRight v-else class="w-5 h-5" />
            </button>
            <div class="bg-blue-50 p-2 rounded">
              <BookOpen class="w-5 h-5 text-blue-900" />
            </div>
            <div class="flex-1">
              <h3 class="font-semibold text-gray-900">{{ course.name }}</h3>
              <div class="flex items-center gap-3 mt-1 flex-wrap">
                <span class="text-sm text-gray-500 flex items-center gap-1">
                  <User class="w-3.5 h-3.5" />
                  {{ course.coordinatorName || 'Sem coordenador' }}
                </span>
                <span class="text-gray-300">•</span>
                <span class="text-sm text-gray-500 flex items-center gap-1">
                  <BookOpen class="w-3.5 h-3.5" />
                  {{ course.subjectCount }} disciplina{{ course.subjectCount !== 1 ? 's' : '' }}
                </span>
                <span class="text-gray-300">•</span>
                <span class="text-sm text-gray-500">{{ course.years }} ano{{ course.years !== 1 ? 's' : '' }}</span>

                <!-- Expected cohorts summary -->
                <template v-if="course.expectedCohortsPerYear && Object.keys(course.expectedCohortsPerYear).length > 0">
                  <span class="text-gray-300">•</span>
                  <div class="flex items-center gap-1 flex-wrap">
                    <span v-for="(count, year) in course.expectedCohortsPerYear" :key="year"
                      class="text-xs px-2 py-0.5 bg-blue-50 text-blue-800 rounded-full border border-blue-100">
                      {{ year }}º: {{ count }}T
                    </span>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <button @click="openEditModal(course)"
              class="p-2 text-gray-400 hover:text-blue-900 hover:bg-blue-50 rounded-lg transition" title="Editar curso">
              <Edit2 class="w-4 h-4" />
            </button>
            <button @click="handleDeleteCourse(course.id)"
              class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition" title="Eliminar curso">
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- Subjects panel -->
        <div v-if="course.expanded" class="border-t border-gray-200 bg-gray-50">
          <div class="p-5">
            <div class="flex items-center justify-between mb-4">
              <h4 class="font-medium text-gray-700">Disciplinas</h4>
              <button @click="openDisciplineModal(course)"
                class="text-blue-900 text-sm flex items-center gap-1 hover:underline">
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
              <div v-for="subject in course.disciplines" :key="subject.id"
                class="bg-white p-4 rounded-lg border border-gray-200 flex items-center justify-between hover:border-blue-200 transition"
                :class="{ 'border-amber-200 bg-amber-50/30': subject.fixedDaySession }">
                <div class="flex-1">
                  <div class="flex items-center gap-2">
                    <p class="font-medium text-gray-900">{{ subject.name }}</p>
                    <span v-if="subject.fixedDaySession"
                      class="inline-flex items-center gap-1 px-2 py-0.5 bg-amber-100 text-amber-700 rounded-full text-xs font-medium">
                      <Calendar class="w-3 h-3" />
                      Quarta-feira · A Equipa
                    </span>
                  </div>
                  <div class="flex items-center gap-3 mt-1 text-sm text-gray-500">
                    <span>{{ subject.credits }} créditos</span>
                    <span>•</span>
                    <span>{{ subject.targetYear }}º Ano</span>
                    <span>•</span>
                    <span>{{ subject.targetSemester }}º Semestre</span>
                  </div>
                  <!-- Professores só para disciplinas normais -->
                  <div v-if="!subject.fixedDaySession" class="mt-2 flex items-center gap-2">
                    <span class="text-sm font-medium text-gray-600">Professores:</span>
                    <div v-if="subject.eligibleTeachers?.length > 0" class="flex flex-wrap gap-1">
                      <span v-for="teacher in subject.eligibleTeachers" :key="teacher.id"
                        class="inline-flex items-center gap-1 px-2 py-0.5 bg-blue-50 text-blue-900 rounded text-xs">
                        <User class="w-3 h-3" />{{ teacher.username }}
                      </span>
                    </div>
                    <span v-else class="text-sm text-gray-400">Nenhum professor elegível</span>
                  </div>
                </div>
                <div class="flex items-center gap-2">
                  <button v-if="!subject.fixedDaySession" @click="openEditDisciplineModal(subject, course)"
                    class="p-2 text-gray-400 hover:text-blue-900 hover:bg-blue-50 rounded-lg transition">
                    <Edit2 class="w-4 h-4" />
                  </button>
                  <button v-if="!subject.fixedDaySession" @click="handleDeleteDiscipline(subject.id, course)"
                    class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition">
                    <Trash2 class="w-4 h-4" />
                  </button>
                  <span v-if="subject.fixedDaySession"
                    class="text-xs text-amber-600 px-2 py-1 bg-amber-50 rounded-lg border border-amber-200">
                    Gerida automaticamente
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="courses.length === 0" class="text-center py-12 bg-white rounded-lg border border-gray-200">
        <BookOpen class="w-12 h-12 text-gray-300 mx-auto mb-3" />
        <p class="text-gray-500">Nenhum curso cadastrado</p>
        <button @click="openCreateModal" class="mt-4 text-blue-900 hover:underline text-sm">
          Criar primeiro curso
        </button>
      </div>
    </div>

    <!-- ── Course Modal ── -->
    <div v-if="showCourseModal" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="closeCourseModal">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">

        <!-- Header -->
        <div class="border-b border-gray-200 p-6">
          <div class="flex items-center gap-3">
            <div :class="editingCourse ? 'bg-amber-100' : 'bg-blue-100'" class="p-2 rounded-lg">
              <Edit v-if="editingCourse" class="w-5 h-5 text-amber-600" />
              <Plus v-else class="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h2 class="text-xl font-semibold text-gray-900">
                {{ editingCourse ? 'Editar Curso' : 'Novo Curso' }}
              </h2>
              <p v-if="editingCourse" class="text-sm text-gray-500 mt-0.5">{{ editingCourse.name }}</p>
            </div>
          </div>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleCourseSubmit" class="p-6 space-y-5">

          <!-- Nome -->
          <div>
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <Tag class="w-4 h-4 text-gray-400" />
              Nome do Curso *
            </label>
            <input v-model="courseForm.name" type="text" required placeholder="Ex: Engenharia Informática"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition" />
          </div>

          <!-- Coordenador -->
          <div>
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <User class="w-4 h-4 text-gray-400" />
              Coordenador *
            </label>
            <div class="relative">
              <select v-model.number="courseForm.coordinatorId" required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg appearance-none focus:ring-2 focus:ring-blue-900 focus:border-transparent transition cursor-pointer">
                <option value="" disabled>Seleccionar coordenador</option>
                <option v-for="c in coordinators" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
              <ChevronDown
                class="w-4 h-4 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Duração -->
          <div>
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <Calendar class="w-4 h-4 text-gray-400" />
              Duração (anos) *
            </label>
            <input v-model.number="courseForm.years" type="number" min="1" max="6" required @change="syncCohortRows"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition" />
          </div>

          <!-- Simulação Empresarial -->
          <div
            class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition">
            <div class="flex items-center gap-3">
              <div class="bg-amber-50 p-2 rounded-lg">
                <BookOpen class="w-4 h-4 text-amber-600" />
              </div>
              <div>
                <p class="text-sm font-medium text-gray-900">Simulação Empresarial</p>
                <p class="text-xs text-gray-500">Inclui Simulação Empresarial I (3º ano) e II (4º ano)</p>
              </div>
            </div>
            <button type="button" @click="courseForm.hasBusinessSimulation = !courseForm.hasBusinessSimulation" :class="courseForm.hasBusinessSimulation
              ? 'bg-amber-500 border-amber-500'
              : 'bg-gray-200 border-gray-200'" class="relative w-11 h-6 rounded-full border-2 transition-colors duration-200">
              <span :class="courseForm.hasBusinessSimulation ? 'translate-x-5' : 'translate-x-0'"
                class="absolute left-0.5 top-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform duration-200" />
            </button>
          </div>

          <!-- Expected cohorts per year -->
          <div>
            <div class="flex items-center justify-between mb-2">
              <label class="flex items-center gap-2 text-sm font-medium text-gray-700">
                <Users class="w-4 h-4 text-gray-400" />
                Turmas Esperadas por Ano Curricular
              </label>
              <span class="text-xs text-gray-400">Máximo de turmas por semestre</span>
            </div>

            <div class="border border-gray-200 rounded-lg overflow-hidden">
              <div class="bg-gray-50 grid grid-cols-2 px-4 py-2 border-b border-gray-200">
                <span class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Ano</span>
                <span class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Turmas / semestre</span>
              </div>
              <div v-for="row in cohortRows" :key="row.year"
                class="grid grid-cols-2 items-center px-4 py-3 border-b border-gray-100 last:border-b-0 hover:bg-gray-50 transition">
                <span class="text-sm font-medium text-gray-700">{{ row.year }}º Ano</span>
                <div class="flex items-center gap-2">
                  <button type="button" @click="row.count = Math.max(0, row.count - 1)"
                    class="w-7 h-7 rounded-md border border-gray-300 flex items-center justify-center text-gray-500 hover:border-blue-900 hover:text-blue-900 transition">
                    <Minus class="w-3 h-3" />
                  </button>
                  <input v-model.number="row.count" type="number" min="0" max="10"
                    class="w-14 text-center px-2 py-1 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-blue-900 focus:border-transparent" />
                  <button type="button" @click="row.count++"
                    class="w-7 h-7 rounded-md border border-gray-300 flex items-center justify-center text-gray-500 hover:border-blue-900 hover:text-blue-900 transition">
                    <PlusIcon class="w-3 h-3" />
                  </button>
                </div>
              </div>
            </div>
            <p class="text-xs text-gray-400 mt-1.5">
              Define quantas turmas são esperadas por semestre para cada ano curricular. Usado na estimativa de geração
              de
              horários.
            </p>
          </div>

          <!-- Actions -->
          <div class="flex gap-3 pt-2">
            <button type="button" @click="closeCourseModal"
              class="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition flex items-center justify-center gap-2">
              <X class="w-4 h-4" />
              Cancelar
            </button>
            <button type="submit"
              class="flex-1 px-4 py-2.5 bg-blue-900 text-white rounded-lg hover:bg-blue-800 transition flex items-center justify-center gap-2">
              <Save v-if="editingCourse" class="w-4 h-4" />
              <Plus v-else class="w-4 h-4" />
              {{ editingCourse ? 'Guardar' : 'Criar' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- ── Discipline Modal (unchanged from original) ── -->
    <div v-if="showDisciplineModal" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b border-gray-200">
          <h2 class="text-xl font-bold text-gray-900">
            {{ editingDiscipline
              ? `Editar Disciplina — ${editingDiscipline.name}`
              : `Nova Disciplina — ${selectedCourse?.name}` }}
          </h2>
        </div>

        <div class="p-6 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nome da Disciplina</label>
            <input v-model="disciplineForm.name" type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent"
              placeholder="Ex: Cálculo I" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Número de Créditos</label>
            <input v-model.number="disciplineForm.credits" type="number" min="1"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent"
              placeholder="Ex: 6" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Ano</label>
              <input v-model.number="disciplineForm.targetYear" type="number" min="1" :max="selectedCourse?.years || 1"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Semestre</label>
              <input v-model.number="disciplineForm.targetSemester" type="number" min="1" max="2"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent" />
            </div>
          </div>

          <!-- Teacher selection -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Professores Elegíveis</label>
            <div class="relative mb-3">
              <input v-model="teacherSearchQuery" type="text"
                class="w-full px-3 py-2 pl-9 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent"
                placeholder="Buscar professores por nome..." />
              <Search class="w-4 h-4 text-gray-400 absolute left-3 top-3" />
            </div>
            <div v-if="disciplineForm.selectedTeachers.length > 0" class="mb-3 flex flex-wrap gap-2">
              <div v-for="teacher in disciplineForm.selectedTeachers" :key="teacher.id"
                class="inline-flex items-center gap-2 px-3 py-1.5 bg-blue-900 text-white rounded-lg text-sm">
                <User class="w-3.5 h-3.5" />
                <span>{{ teacher.username }}</span>
                <button @click="removeTeacher(teacher.id)" class="hover:bg-blue-800 rounded p-0.5">
                  <X class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
            <div class="border border-gray-300 rounded-lg max-h-48 overflow-y-auto">
              <div v-if="loadingTeachers" class="p-4 text-center text-gray-500 text-sm">A carregar professores...</div>
              <div v-else-if="filteredTeachers.length === 0" class="p-4 text-center text-gray-400 text-sm">Nenhum
                professor
                encontrado</div>
              <div v-else>
                <button v-for="teacher in filteredTeachers" :key="teacher.id" type="button"
                  @click="toggleTeacher(teacher)"
                  class="w-full px-4 py-2.5 flex items-center justify-between hover:bg-gray-50 border-b border-gray-100 last:border-b-0 transition"
                  :class="{ 'bg-blue-50': isTeacherSelected(teacher.id) }">
                  <div class="flex items-center gap-3">
                    <div class="w-5 h-5 rounded border-2 flex items-center justify-center"
                      :class="isTeacherSelected(teacher.id) ? 'border-blue-900 bg-blue-900' : 'border-gray-300'">
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

        <div class="p-6 border-t border-gray-200 flex justify-end gap-3">
          <button type="button" @click="closeDisciplineModal"
            class="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition">
            Cancelar
          </button>
          <button type="button" @click="submitDiscipline" :disabled="!isDisciplineFormValid"
            class="px-4 py-2 bg-blue-900 text-white rounded-lg hover:bg-blue-800 transition disabled:opacity-50 disabled:cursor-not-allowed">
            {{ editingDiscipline ? 'Actualizar' : 'Criar' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { courseService } from '@/services/courseService'
import { subjectService } from '@/services/subjectService'
import { teacherService } from '@/services/teacherService'
import { useToast } from '@/composables/useToast'
import type { CourseListResponse, CoordinatorOption } from '@/services/dto/course'
import {
  GraduationCap, Plus, Edit, Edit2, Trash2,
  ChevronDown, ChevronRight, BookOpen, User,
  Tag, Calendar, Users, Minus, X, Save,
  Check, Search, ChevronDown as ChevronDownIcon,
} from 'lucide-vue-next'
import { Plus as PlusIcon } from 'lucide-vue-next'

const toast = useToast()

// ── State ──────────────────────────────────────────────────────────
const courses = ref<any[]>([])
const coordinators = ref<CoordinatorOption[]>([])
const teachers = ref<any[]>([])
const loading = ref(false)
const loadingTeachers = ref(false)
const showCourseModal = ref(false)
const showDisciplineModal = ref(false)
const editingCourse = ref<any>(null)
const selectedCourse = ref<any>(null)
const editingDiscipline = ref<any>(null)
const teacherSearchQuery = ref('')

// Cohort rows drive the expectedCohortsPerYear map in the form
const cohortRows = ref<{ year: number; count: number }[]>([])

const courseForm = reactive({
  name: '',
  coordinatorId: '' as number | '',
  years: 4,
  hasBusinessSimulation: false,  // ← NOVO
})

const disciplineForm = ref({
  name: '',
  credits: 0,
  targetYear: 1,
  targetSemester: 1,
  selectedTeachers: [] as any[],
})

// ── Computed ───────────────────────────────────────────────────────
const filteredTeachers = computed(() => {
  const q = teacherSearchQuery.value.toLowerCase().trim()
  if (!q) return teachers.value
  return teachers.value.filter(t =>
    t.username.toLowerCase().includes(q) || t.email.toLowerCase().includes(q)
  )
})

const isDisciplineFormValid = computed(() =>
  disciplineForm.value.name.trim() !== '' &&
  disciplineForm.value.credits > 0 &&
  disciplineForm.value.targetYear > 0 &&
  disciplineForm.value.targetSemester > 0 &&
  disciplineForm.value.selectedTeachers.length > 0
)

// Builds the map from cohortRows for the API payload
function buildExpectedCohortsMap(): Record<number, number> {
  const map: Record<number, number> = {}
  for (const row of cohortRows.value) {
    if (row.count > 0) map[row.year] = row.count
  }
  return map
}

// Syncs cohortRows array whenever `years` changes
function syncCohortRows() {
  const years = courseForm.years || 0
  const existing = new Map(cohortRows.value.map(r => [r.year, r.count]))
  cohortRows.value = Array.from({ length: years }, (_, i) => ({
    year: i + 1,
    count: existing.get(i + 1) ?? 0,
  }))
}

// ── Data loading ───────────────────────────────────────────────────
async function loadCourses() {
  loading.value = true
  try {
    const page = await courseService.getAll(0, 50)
    courses.value = page.content.map((c: CourseListResponse) => ({
      ...c,
      disciplines: [],
      expanded: false,
      loadingSubjects: false,
    }))
  } catch {
    toast.error('Erro ao carregar cursos.')
  } finally {
    loading.value = false
  }
}

async function loadCoordinators() {
  try {
    const page = await courseService.getCoordinators()
    coordinators.value = page.content
  } catch {
    coordinators.value = []
  }
}

async function loadTeachers() {
  loadingTeachers.value = true
  try {
    const page = await teacherService.getAll(0, 1000)
    teachers.value = page.content
  } finally {
    loadingTeachers.value = false
  }
}

onMounted(() => Promise.all([loadCourses(), loadCoordinators()]))

// ── Course expand ──────────────────────────────────────────────────
async function toggleCourse(course: any) {
  course.expanded = !course.expanded
  if (course.expanded && course.disciplines.length === 0) {
    await loadSubjects(course)
  }
}

async function loadSubjects(course: any, force = false) {
  if (course.loadingSubjects || (!force && course.disciplines.length > 0)) return
  course.loadingSubjects = true
  try {
    const page = await subjectService.getAllByCourse(course.id, 0, 100)
    course.disciplines = page.content
  } finally {
    course.loadingSubjects = false
  }
}

// ── Course modal ───────────────────────────────────────────────────
function openCreateModal() {
  editingCourse.value = null
  courseForm.name = ''
  courseForm.coordinatorId = ''
  courseForm.years = 4
  cohortRows.value = Array.from({ length: 4 }, (_, i) => ({ year: i + 1, count: 0 }))
  loadCoordinators()
  showCourseModal.value = true
}

function openEditModal(course: any) {
  editingCourse.value = course
  courseForm.name = course.name
  courseForm.coordinatorId = course.coordinatorId
  courseForm.years = course.years
  // Populate cohortRows from existing map
  const map: Record<number, number> = course.expectedCohortsPerYear ?? {}
  courseForm.hasBusinessSimulation = course.hasBusinessSimulation ?? false
  cohortRows.value = Array.from({ length: course.years }, (_, i) => ({
    year: i + 1,
    count: map[i + 1] ?? 0,
  }))
  loadCoordinators()
  showCourseModal.value = true
}

function closeCourseModal() {
  showCourseModal.value = false
  editingCourse.value = null
}

async function handleCourseSubmit() {
const payload = {
  name: courseForm.name,
  coordinatorId: courseForm.coordinatorId as number,
  years: courseForm.years,
  expectedCohortsPerYear: buildExpectedCohortsMap(),
  hasBusinessSimulation: courseForm.hasBusinessSimulation,  // ← NOVO
}
  try {
    if (editingCourse.value) {
      await courseService.update(editingCourse.value.id, payload)
      toast.success('Curso actualizado com sucesso!')
    } else {
      await courseService.create(payload)
      toast.success('Curso criado com sucesso!')
    }
    closeCourseModal()
    await loadCourses()
  } catch {
    toast.error('Erro ao guardar curso.')
  }
}

async function handleDeleteCourse(id: number) {
  if (!confirm('Tem a certeza que deseja eliminar este curso?')) return
  try {
    await courseService.delete(id)
    toast.success('Curso eliminado com sucesso!')
    await loadCourses()
  } catch {
    toast.error('Erro ao eliminar curso.')
  }
}

// ── Discipline modal ───────────────────────────────────────────────
function resetDisciplineForm() {
  disciplineForm.value = { name: '', credits: 0, targetYear: 1, targetSemester: 1, selectedTeachers: [] }
  teacherSearchQuery.value = ''
}

async function openDisciplineModal(course: any) {
  selectedCourse.value = course
  editingDiscipline.value = null
  resetDisciplineForm()
  if (teachers.value.length === 0) await loadTeachers()
  showDisciplineModal.value = true
}

async function openEditDisciplineModal(discipline: any, course: any) {
  selectedCourse.value = course
  editingDiscipline.value = { ...discipline }
  if (teachers.value.length === 0) await loadTeachers()
  disciplineForm.value = {
    name: discipline.name,
    credits: discipline.credits,
    targetYear: discipline.targetYear,
    targetSemester: discipline.targetSemester,
    selectedTeachers: (discipline.eligibleTeachers ?? []).map((t: any) => ({ ...t })),
  }
  showDisciplineModal.value = true
}

function closeDisciplineModal() {
  showDisciplineModal.value = false
  editingDiscipline.value = null
  selectedCourse.value = null
  resetDisciplineForm()
}

async function submitDiscipline() {
  if (!isDisciplineFormValid.value) return
  const data = {
    name: disciplineForm.value.name,
    credits: disciplineForm.value.credits,
    targetYear: disciplineForm.value.targetYear,
    targetSemester: disciplineForm.value.targetSemester,
    courseId: selectedCourse.value.id,
    eligibleTeacherIds: disciplineForm.value.selectedTeachers.map((t: any) => t.id),
  }
  try {
    if (editingDiscipline.value) {
      await subjectService.update(editingDiscipline.value.id, data)
      toast.success('Disciplina actualizada!')
    } else {
      await subjectService.create(data)
      const idx = courses.value.findIndex(c => c.id === selectedCourse.value.id)
      if (idx !== -1) courses.value[idx].subjectCount++
      toast.success('Disciplina criada!')
    }
    closeDisciplineModal()
    await loadSubjects(selectedCourse.value, true)
  } catch {
    toast.error('Erro ao guardar disciplina.')
  }
}

async function handleDeleteDiscipline(id: number, course: any) {
  if (!confirm('Tem a certeza que deseja eliminar esta disciplina?')) return
  try {
    await subjectService.delete(id)
    await loadSubjects(course, true)
    const idx = courses.value.findIndex(c => c.id === course.id)
    if (idx !== -1) courses.value[idx].subjectCount--
    toast.success('Disciplina eliminada!')
  } catch {
    toast.error('Erro ao eliminar disciplina.')
  }
}

// ── Teacher helpers ────────────────────────────────────────────────
const isTeacherSelected = (id: number) => disciplineForm.value.selectedTeachers.some(t => t.id === id)

function toggleTeacher(teacher: any) {
  const idx = disciplineForm.value.selectedTeachers.findIndex(t => t.id === teacher.id)
  if (idx > -1) disciplineForm.value.selectedTeachers.splice(idx, 1)
  else disciplineForm.value.selectedTeachers.push(teacher)
}

function removeTeacher(id: number) {
  const idx = disciplineForm.value.selectedTeachers.findIndex(t => t.id === id)
  if (idx > -1) disciplineForm.value.selectedTeachers.splice(idx, 1)
}
</script>
