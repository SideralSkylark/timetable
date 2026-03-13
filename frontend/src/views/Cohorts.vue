<template>
  <div class="min-h-screen bg-gray-50 p-6">

    <!-- Header -->
    <div class="max-w-6xl mx-auto mb-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="bg-blue-900 p-2.5 rounded-lg">
              <UsersIcon class="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 class="text-xl font-semibold text-gray-900">Turmas</h1>
              <p class="text-gray-400 text-sm">Confirmar ingressos e gerir turmas</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <!-- Confirmation progress pill -->
            <div v-if="confirmationProgress"
              class="flex items-center gap-2 px-3 py-1.5 bg-amber-50 border border-amber-100 rounded-lg text-xs text-amber-700 font-medium">
              <AlertCircle class="w-3.5 h-3.5" />
              {{ confirmationProgress.confirmed }}/{{ confirmationProgress.total }} confirmadas
            </div>
            <button @click="openCreateModal"
              class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition text-sm font-medium">
              <Plus class="w-4 h-4" />
              Nova turma
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="max-w-6xl mx-auto">
      <!-- Delete confirmation banner -->
      <div v-if="confirmDeleteId !== null"
        class="mb-3 flex items-center justify-between bg-red-50 border border-red-100 rounded-lg px-4 py-3">
        <span class="text-sm text-red-700">Tem a certeza que quer eliminar esta turma?</span>
        <div class="flex gap-2">
          <button @click="confirmDeleteId = null"
            class="px-3 py-1.5 text-xs border border-gray-200 text-gray-500 rounded-md hover:bg-white transition">
            Cancelar
          </button>
          <button @click="handleDelete(confirmDeleteId!)"
            class="px-3 py-1.5 text-xs bg-red-600 text-white rounded-md hover:bg-red-700 transition font-medium">
            Eliminar
          </button>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-100">
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Turma</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Curso</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Ano lectivo</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Semestre</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Alunos</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Estado</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody>
            <tr v-if="mappedCohorts.length === 0">
              <td colspan="7" class="text-center py-16 text-gray-400 text-sm">
                <UsersIcon class="w-8 h-8 mx-auto mb-3 text-gray-300" />
                Nenhuma turma encontrada
              </td>
            </tr>
            <tr
              v-for="cohort in mappedCohorts"
              :key="cohort.id"
              class="border-b border-gray-50 last:border-0 hover:bg-gray-50 transition-colors group"
              :class="cohort.status === 'ESTIMATED' ? 'bg-amber-50/20' : ''"
            >
              <td class="px-5 py-3.5 font-medium text-gray-800">{{ cohort.turma }}</td>
              <td class="px-5 py-3.5 text-gray-500">{{ cohort.courseName }}</td>
              <td class="px-5 py-3.5 text-gray-500">{{ cohort.academicYear }}</td>
              <td class="px-5 py-3.5 text-gray-500">{{ cohort.semester }}º sem.</td>
              <td class="px-5 py-3.5">
                <span :class="cohort.status === 'ESTIMATED' ? 'text-amber-600 italic' : 'text-gray-700'">
                  {{ cohort.studentCount }}
                  <span v-if="cohort.status === 'ESTIMATED'" class="text-xs not-italic text-amber-500">(est.)</span>
                </span>
              </td>
              <td class="px-5 py-3.5">
                <span :class="statusBadgeClass(cohort.status)"
                  class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium">
                  <Clock v-if="cohort.status === 'ESTIMATED'" class="w-3 h-3" />
                  <CheckCircle v-else-if="cohort.status === 'CONFIRMED'" class="w-3 h-3" />
                  {{ statusLabels[cohort.status] ?? cohort.status }}
                </span>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center justify-end gap-2">
                  <!-- Confirm enrolments (estimated cohorts) -->
                  <button v-if="cohort.status === 'ESTIMATED'"
                    @click="openConfirmModal(cohort)"
                    class="flex items-center gap-1 px-2.5 py-1 bg-green-600 text-white text-xs rounded-md hover:bg-green-700 transition opacity-0 group-hover:opacity-100">
                    <CheckCircle class="w-3 h-3" />
                    Confirmar
                  </button>
                  <!-- Edit (confirmed cohorts) -->
                  <button v-else
                    @click="openEditModal(cohort)"
                    class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-amber-600 hover:border-amber-200 hover:bg-amber-50 transition opacity-0 group-hover:opacity-100">
                    <Edit class="w-3.5 h-3.5" />
                  </button>
                  <!-- Delete -->
                  <button
                    @click="confirmDeleteId = cohort.id"
                    class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-red-600 hover:border-red-200 hover:bg-red-50 transition opacity-0 group-hover:opacity-100">
                    <Trash2 class="w-3.5 h-3.5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div class="border-t border-gray-100">
          <Pagination
            :page="currentPage"
            :totalPages="cohortStore.cohortsPage?.page.totalPages ?? 1"
            @update:page="fetchCohorts"
          />
        </div>
      </div>
    </div>

    <!-- Modal: Confirm enrolments -->
    <div v-if="showConfirmModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
      @click.self="showConfirmModal = false">
      <div class="bg-white rounded-xl shadow-2xl max-w-md w-full border border-gray-100">

        <div class="p-5 border-b border-gray-100 flex items-center gap-3">
          <div class="bg-green-50 p-2 rounded-lg">
            <CheckCircle class="w-4 h-4 text-green-600" />
          </div>
          <div>
            <h2 class="text-base font-semibold text-gray-900">Confirmar ingressos</h2>
            <p class="text-xs text-gray-400 mt-0.5">
              {{ confirmingCohort?.turma }} · {{ confirmingCohort?.courseName }}
            </p>
          </div>
        </div>

        <div class="p-5 space-y-4">
          <!-- Info banner -->
          <div class="flex items-start gap-2.5 bg-amber-50 border border-amber-100 rounded-lg px-3 py-2.5">
            <AlertCircle class="w-4 h-4 text-amber-500 mt-0.5 shrink-0" />
            <p class="text-xs text-amber-700">
              Estimativa inicial: <strong>{{ confirmingCohort?.studentCount }} alunos</strong>.
              Insira o número real de ingressos confirmados.
            </p>
          </div>

          <!-- Input -->
          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <UsersIcon class="w-3.5 h-3.5" />
              Número de ingressos <span class="text-blue-900">*</span>
            </label>
            <input
              v-model.number="confirmForm.studentCount"
              type="number"
              min="1"
              :max="cohortStore.maxRoomCapacity ?? 200"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800"
            />
            <p v-if="cohortStore.maxRoomCapacity && confirmForm.studentCount > cohortStore.maxRoomCapacity"
              class="text-xs text-red-500 mt-1.5 flex items-center gap-1">
              <AlertCircle class="w-3 h-3" />
              Excede a capacidade máxima das salas ({{ cohortStore.maxRoomCapacity }} alunos).
              Considere criar uma turma adicional.
            </p>
            <p v-else class="text-xs text-gray-400 mt-1.5">
              Capacidade máxima das salas: {{ cohortStore.maxRoomCapacity }} alunos
            </p>
          </div>

          <div class="flex gap-2 pt-1">
            <button type="button" @click="showConfirmModal = false"
              class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center justify-center gap-1.5">
              <X class="w-3.5 h-3.5" />
              Cancelar
            </button>
            <button @click="handleConfirm"
              class="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg text-sm hover:bg-green-700 transition flex items-center justify-center gap-1.5 font-medium">
              <CheckCircle class="w-3.5 h-3.5" />
              Confirmar ingressos
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Create / Edit -->
    <div v-if="showModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
      @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-2xl max-w-md w-full max-h-[90vh] overflow-y-auto border border-gray-100">

        <div class="p-5 border-b border-gray-100 flex items-center gap-3">
          <div :class="isEditing ? 'bg-amber-50' : 'bg-blue-50'" class="p-2 rounded-lg">
            <Edit v-if="isEditing" class="w-4 h-4 text-amber-600" />
            <UserPlus v-else class="w-4 h-4 text-blue-900" />
          </div>
          <h2 class="text-base font-semibold text-gray-900">
            {{ isEditing ? 'Editar turma' : 'Nova turma' }}
          </h2>
        </div>

        <!-- Loading state -->
        <div v-if="loadingDetail" class="flex items-center justify-center py-12">
          <Loader2 class="w-5 h-5 animate-spin text-blue-900" />
        </div>

        <form v-else @submit.prevent="handleSubmit" class="p-5 space-y-4">

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Ano curricular <span class="text-blue-900">*</span>
              </label>
              <input v-model.number="form.year" type="number" min="1" max="5" required
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800" />
            </div>
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Secção <span class="text-blue-900">*</span>
              </label>
              <input v-model="form.section" type="text" required maxlength="2"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800 uppercase" />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Ano lectivo <span class="text-blue-900">*</span>
              </label>
              <input v-model.number="form.academicYear" type="number" required
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800" />
            </div>
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Semestre <span class="text-blue-900">*</span>
              </label>
              <select v-model.number="form.semester" required
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800">
                <option value="" disabled>Selecionar</option>
                <option :value="1">1º semestre</option>
                <option :value="2">2º semestre</option>
              </select>
            </div>
          </div>

          <div v-if="!isEditing">
            <label class="text-xs font-medium text-gray-500 mb-1.5 block">
              Curso <span class="text-blue-900">*</span>
            </label>
            <select v-model.number="form.courseId" required
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800">
              <option value="" disabled>Selecionar curso</option>
              <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
                {{ course.name }}
              </option>
            </select>
          </div>

          <div class="flex gap-2 pt-1">
            <button type="button" @click="closeModal"
              class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center justify-center gap-1.5">
              <X class="w-3.5 h-3.5" />
              Cancelar
            </button>
            <button type="submit"
              class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg text-sm hover:bg-blue-800 transition flex items-center justify-center gap-1.5 font-medium">
              <Check class="w-3.5 h-3.5" />
              {{ isEditing ? 'Guardar alterações' : 'Criar turma' }}
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
import Pagination from '@/component/ui/Pagination.vue'
import {
  Users as UsersIcon,
  Plus,
  Edit,
  UserPlus,
  CheckCircle,
  Clock,
  AlertCircle,
  Trash2,
  Loader2,
  X,
  Check,
} from 'lucide-vue-next'

const cohortStore = useCohortStore()
const courseStore = useCourseStore()
const toast = useToast()

const currentPage = ref(0)
const showModal = ref(false)
const showConfirmModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const loadingDetail = ref(false)
const confirmDeleteId = ref<number | null>(null)

const confirmingCohort = ref<(CohortListResponse & { turma: string }) | null>(null)
const confirmForm = reactive({ studentCount: 35 })

const form = reactive({
  year: 1,
  section: '',
  academicYear: new Date().getFullYear(),
  semester: 1 as number | '',
  courseId: '' as number | '',
  studentIds: [] as number[],
})

// ── Computed ─────────────────────────────────────────────────────────
const mappedCohorts = computed(() =>
  (cohortStore.cohortsPage?.content ?? []).map((c: CohortListResponse) => ({
    ...c,
    turma: `${c.year}º Ano · Turma ${c.section}`,
  }))
)

const confirmationProgress = computed(() => {
  const all = cohortStore.cohortsPage?.content ?? []
  if (all.length === 0) return null
  const confirmed = all.filter(c => c.status !== 'ESTIMATED').length
  return { confirmed, total: all.length }
})

const statusLabels: Record<string, string> = {
  ESTIMATED: 'Estimado',
  CONFIRMED: 'Confirmado',
  ACTIVE:    'Activo',
  ARCHIVED:  'Arquivado',
}

const statusBadgeClass = (status: string) => ({
  ESTIMATED: 'bg-amber-100 text-amber-700',
  CONFIRMED: 'bg-green-100 text-green-700',
  ACTIVE:    'bg-blue-100 text-blue-700',
  ARCHIVED:  'bg-gray-100 text-gray-500',
}[status] ?? 'bg-gray-100 text-gray-500')

// ── Lifecycle ─────────────────────────────────────────────────────────
onMounted(() => fetchCohorts())

async function fetchCohorts(page = 0) {
  currentPage.value = page
  await cohortStore.fetchCohorts(page, 10)
}

// ── Confirm enrolments ────────────────────────────────────────────────
async function openConfirmModal(cohort: CohortListResponse & { turma: string }) {
  confirmingCohort.value = cohort
  confirmForm.studentCount = cohort.studentCount
  await cohortStore.fetchMaxRoomCapacity()
  showConfirmModal.value = true
}

async function handleConfirm() {
  if (!confirmingCohort.value) return
  try {
    await cohortStore.confirmCohort(confirmingCohort.value.id, confirmForm.studentCount)
    toast.success('Ingressos confirmados!')
    showConfirmModal.value = false
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao confirmar ingressos.')
  }
}

// ── Create / Edit modal ───────────────────────────────────────────────
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
  await cohortStore.fetchCohort(row.id)
  const detail = cohortStore.selectedCohort
  if (detail) {
    form.year = detail.year
    form.section = detail.section
    form.academicYear = detail.academicYear
    form.semester = detail.semester
    form.courseId = detail.courseId
    form.studentIds = detail.studentIds ?? []
  }
  loadingDetail.value = false
}

function closeModal() {
  showModal.value = false
  isEditing.value = false
  editingId.value = null
}

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
  try {
    await cohortStore.deleteCohort(id)
    confirmDeleteId.value = null
    toast.success('Turma eliminada com sucesso!')
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao eliminar turma.')
  }
}
</script>
