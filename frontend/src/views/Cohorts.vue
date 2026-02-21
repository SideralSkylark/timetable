<template>
  <div class="min-h-screen bg-gray-50 p-6">

    <!-- Header -->
    <div class="max-w-6xl mx-auto mb-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="bg-blue-900 p-3 rounded-lg">
              <Users class="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">Turmas</h1>
              <p class="text-gray-500 text-sm">Gerir as turmas da instituição</p>
            </div>
          </div>
          <button
            @click="openCreateModal"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition"
          >
            <Plus class="w-5 h-5" />
            Nova Turma
          </button>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="max-w-6xl mx-auto">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <CrudTable
          :columns="tableColumns"
          :rows="mappedCohorts"
          :currentPage="currentPage"
          :totalPages="cohortStore.cohortsPage?.page.totalPages ?? 0"
          @edit="openEditModal"
          @delete="handleDelete"
          @change-page="fetchCohorts"
        />
      </div>
    </div>

    <!-- Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="closeModal"
    >
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">

        <!-- Modal header -->
        <div class="border-b border-gray-200 p-6">
          <div class="flex items-center gap-3">
            <div :class="isEditing ? 'bg-amber-100' : 'bg-blue-100'" class="p-2 rounded-lg">
              <Edit v-if="isEditing" class="w-5 h-5 text-amber-600" />
              <UserPlus v-else class="w-5 h-5 text-blue-600" />
            </div>
            <h2 class="text-xl font-semibold text-gray-900">
              {{ isEditing ? 'Editar Turma' : 'Nova Turma' }}
            </h2>
          </div>
          <p v-if="isEditing && editingId" class="text-sm text-gray-500 mt-2 ml-11">
            A carregar detalhes...
          </p>
        </div>

        <!-- Loading detail -->
        <div v-if="loadingDetail" class="flex items-center justify-center py-12">
          <Loader2 class="w-6 h-6 animate-spin text-blue-900" />
        </div>

        <!-- Form -->
        <form v-else @submit.prevent="handleSubmit" class="p-6 space-y-5">

          <!-- Ano Curricular + Secção -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
                <GraduationCap class="w-4 h-4 text-gray-500" />
                Ano Curricular *
              </label>
              <input
                v-model.number="form.year"
                type="number"
                min="1"
                max="5"
                required
                placeholder="Ex: 1"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition"
              />
            </div>
            <div>
              <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
                <Tag class="w-4 h-4 text-gray-500" />
                Secção *
              </label>
              <input
                v-model="form.section"
                type="text"
                required
                maxlength="2"
                placeholder="Ex: A"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition uppercase"
              />
            </div>
          </div>

          <!-- Ano Lectivo + Semestre -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
                <Calendar class="w-4 h-4 text-gray-500" />
                Ano Lectivo *
              </label>
              <input
                v-model.number="form.academicYear"
                type="number"
                required
                placeholder="Ex: 2026"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition"
              />
            </div>
            <div>
              <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
                <BookOpen class="w-4 h-4 text-gray-500" />
                Semestre *
              </label>
              <div class="relative">
                <select
                  v-model.number="form.semester"
                  required
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg appearance-none focus:ring-2 focus:ring-blue-900 focus:border-transparent transition cursor-pointer"
                >
                  <option value="" disabled>Seleccionar</option>
                  <option :value="1">1º Semestre</option>
                  <option :value="2">2º Semestre</option>
                </select>
                <ChevronDown class="w-4 h-4 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              </div>
            </div>
          </div>

          <!-- Curso (só em criação) -->
          <div v-if="!isEditing">
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <School class="w-4 h-4 text-gray-500" />
              Curso *
            </label>
            <div class="relative">
              <select
                v-model.number="form.courseId"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg appearance-none focus:ring-2 focus:ring-blue-900 focus:border-transparent transition cursor-pointer"
              >
                <option value="" disabled>Seleccionar curso</option>
                <option
                  v-for="course in courseStore.courses"
                  :key="course.id"
                  :value="course.id"
                >
                  {{ course.name }}
                </option>
              </select>
              <ChevronDown class="w-4 h-4 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-3 pt-2">
            <button
              type="button"
              @click="closeModal"
              class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition flex items-center justify-center gap-2"
            >
              <X class="w-4 h-4" />
              Cancelar
            </button>
            <button
              type="submit"
              class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg hover:bg-blue-800 transition flex items-center justify-center gap-2"
            >
              <Save v-if="isEditing" class="w-4 h-4" />
              <Plus v-else class="w-4 h-4" />
              {{ isEditing ? 'Guardar' : 'Criar' }}
            </button>
          </div>

        </form>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useCohortStore } from '@/stores/cohorts'
import { useCourseStore } from '@/stores/course'
import { useToast } from '@/composables/useToast'
import type { CohortListResponse } from '@/services/dto/cohort'
import CrudTable from '@/component/ui/CrudTable.vue'
import {
  Users, Plus, Edit, UserPlus, GraduationCap,
  Tag, Calendar, BookOpen, School, ChevronDown,
  X, Save, Loader2,
} from 'lucide-vue-next'

const cohortStore = useCohortStore()
const courseStore = useCourseStore()
const toast = useToast()

const currentPage = ref(0)
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const loadingDetail = ref(false)

const form = reactive({
  year: 1,
  section: '',
  academicYear: new Date().getFullYear(),
  semester: 1 as number | '',
  courseId: '' as number | '',
  studentIds: [] as number[],
})

// ── Table ──────────────────────────────────────────────────────────
const tableColumns = [
  { key: 'turma',        label: 'Turma' },
  { key: 'courseName',   label: 'Curso' },
  { key: 'academicYear', label: 'Ano Lectivo' },
  { key: 'semesterLabel', label: 'Semestre' },
  { key: 'studentCount', label: 'Estudantes' },
  { key: 'statusLabel',  label: 'Estado' },
]

const statusLabels: Record<string, string> = {
  ESTIMATED: 'Estimado',
  CONFIRMED: 'Confirmado',
  ACTIVE: 'Activo',
  ARCHIVED: 'Arquivado',
}

const mappedCohorts = computed(() =>
  (cohortStore.cohortsPage?.content ?? []).map((c: CohortListResponse) => ({
    ...c,
    turma: `${c.year}º Ano · Turma ${c.section}`,
    semesterLabel: `${c.semester}º Semestre`,
    statusLabel: statusLabels[c.status] ?? c.status,
  }))
)

// ── Lifecycle ──────────────────────────────────────────────────────
onMounted(() => fetchCohorts())

async function fetchCohorts(page = 0) {
  currentPage.value = page
  await cohortStore.fetchCohorts(page, 10)
}

// ── Modal ──────────────────────────────────────────────────────────
async function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  form.year = 1
  form.section = ''
  form.academicYear = new Date().getFullYear()
  form.semester = 1
  form.courseId = ''
  form.studentIds = []
  await courseStore.fetchAllCoursesSimple()
  showModal.value = true
}

async function openEditModal(row: CohortListResponse) {
  isEditing.value = true
  editingId.value = row.id
  showModal.value = true
  loadingDetail.value = true

  // Fetch full detail to get studentIds for the PUT request
  await cohortStore.fetchCohort(row.id)
  const detail = cohortStore.selectedCohort

  if (detail) {
    form.year = detail.year
    form.section = detail.section
    form.academicYear = detail.academicYear
    form.semester = detail.semester
    form.courseId = detail.courseId
    form.studentIds = detail.studentIds
  }

  loadingDetail.value = false
}

function closeModal() {
  showModal.value = false
  isEditing.value = false
  editingId.value = null
}

// ── CRUD ───────────────────────────────────────────────────────────
async function handleSubmit() {
  isEditing.value ? await handleUpdate() : await handleCreate()
}

async function handleCreate() {
  try {
    await cohortStore.createCohort({
      year: form.year,
      section: form.section.toUpperCase(),
      academicYear: form.academicYear,
      semester: form.semester as number,
      courseId: form.courseId as number,
      studentIds: [],
    })
    toast.success('Turma criada com sucesso!')
    closeModal()
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao criar turma.')
  }
}

async function handleUpdate() {
  if (!editingId.value) return
  try {
    await cohortStore.updateCohort(editingId.value, {
      year: form.year,
      section: form.section.toUpperCase(),
      academicYear: form.academicYear,
      semester: form.semester as number,
      studentIds: form.studentIds,
    })
    toast.success('Turma actualizada com sucesso!')
    closeModal()
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao actualizar turma.')
  }
}

async function handleDelete(id: number) {
  if (!confirm('Tem a certeza que deseja eliminar esta turma?')) return
  try {
    await cohortStore.deleteCohort(id)
    toast.success('Turma eliminada com sucesso!')
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao eliminar turma.')
  }
}
</script>
