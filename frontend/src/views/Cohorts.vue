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
              <p class="text-gray-500 text-sm">
                Confirmar ingressos e gerir turmas
              </p>
            </div>
          </div>
          <div class="flex gap-2">
            <!-- Banner de progresso de confirmação -->
            <div v-if="confirmationProgress"
              class="flex items-center gap-2 px-3 py-2 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-700">
              <AlertCircle class="w-4 h-4" />
              {{ confirmationProgress.confirmed }}/{{ confirmationProgress.total }} turmas confirmadas
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
    </div>

    <!-- Tabela -->
    <div class="max-w-6xl mx-auto">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Turma</th>
              <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Curso</th>
              <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Ano Lectivo</th>
              <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Semestre</th>
              <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Alunos</th>
              <th class="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
              <th class="text-right px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Acções</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="cohort in mappedCohorts" :key="cohort.id"
              :class="cohort.status === 'ESTIMATED' ? 'bg-amber-50/30' : 'bg-white'">
              <td class="px-6 py-4 text-sm font-medium text-gray-900">{{ cohort.turma }}</td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ cohort.courseName }}</td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ cohort.academicYear }}</td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ cohort.semester }}º Semestre</td>
              <td class="px-6 py-4 text-sm text-gray-600">
                <span :class="cohort.status === 'ESTIMATED' ? 'text-amber-600 italic' : 'text-gray-900'">
                  {{ cohort.studentCount }}
                  <span v-if="cohort.status === 'ESTIMATED'" class="text-xs">(est.)</span>
                </span>
              </td>
              <td class="px-6 py-4">
                <span :class="statusBadgeClass(cohort.status)"
                  class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium">
                  <Clock v-if="cohort.status === 'ESTIMATED'" class="w-3 h-3" />
                  <CheckCircle v-else-if="cohort.status === 'CONFIRMED'" class="w-3 h-3" />
                  {{ statusLabels[cohort.status] ?? cohort.status }}
                </span>
              </td>
              <td class="px-6 py-4 text-right">
                <div class="flex items-center justify-end gap-2">
                  <!-- Confirmar ingressos (turmas estimadas) -->
                  <button v-if="cohort.status === 'ESTIMATED'"
                    @click="openConfirmModal(cohort)"
                    class="flex items-center gap-1 px-3 py-1.5 bg-green-600 text-white text-xs rounded-lg hover:bg-green-700 transition">
                    <CheckCircle class="w-3.5 h-3.5" />
                    Confirmar
                  </button>
                  <!-- Editar (turmas confirmadas) -->
                  <button v-else
                    @click="openEditModal(cohort)"
                    class="p-1.5 text-gray-400 hover:text-amber-600 hover:bg-amber-50 rounded transition">
                    <Edit class="w-4 h-4" />
                  </button>
                  <!-- Eliminar -->
                  <button
                    @click="handleDelete(cohort.id)"
                    class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition">
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="mappedCohorts.length === 0">
              <td colspan="7" class="px-6 py-12 text-center text-gray-400 text-sm">
                Nenhuma turma encontrada
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Paginação -->
        <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
          <p class="text-sm text-gray-500">
            Página {{ currentPage + 1 }} de {{ cohortStore.cohortsPage?.page.totalPages ?? 1 }}
          </p>
          <div class="flex gap-2">
            <button :disabled="currentPage === 0"
              @click="fetchCohorts(currentPage - 1)"
              class="px-3 py-1.5 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50 transition">
              Anterior
            </button>
            <button :disabled="currentPage + 1 >= (cohortStore.cohortsPage?.page.totalPages ?? 1)"
              @click="fetchCohorts(currentPage + 1)"
              class="px-3 py-1.5 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50 transition">
              Seguinte
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Confirmar Ingressos -->
    <div v-if="showConfirmModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="showConfirmModal = false">
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full">
        <div class="border-b border-gray-200 p-6">
          <div class="flex items-center gap-3">
            <div class="bg-green-100 p-2 rounded-lg">
              <CheckCircle class="w-5 h-5 text-green-600" />
            </div>
            <div>
              <h2 class="text-xl font-semibold text-gray-900">Confirmar Ingressos</h2>
              <p class="text-sm text-gray-500 mt-0.5">{{ confirmingCohort?.turma }} · {{ confirmingCohort?.courseName }}</p>
            </div>
          </div>
        </div>

        <div class="p-6 space-y-4">
          <div class="bg-amber-50 border border-amber-200 rounded-lg p-3 text-sm text-amber-700">
            Estimativa inicial: <strong>{{ confirmingCohort?.studentCount }} alunos</strong>.
            Insira o número real de ingressos confirmados.
          </div>

<input
  v-model.number="confirmForm.studentCount"
  type="number"
  min="1"
  :max="cohortStore.maxRoomCapacity ?? 200"
/>
<p v-if="cohortStore.maxRoomCapacity && confirmForm.studentCount > cohortStore.maxRoomCapacity"
   class="text-xs text-red-500 mt-1 flex items-center gap-1">
  <AlertCircle class="w-3 h-3" />
  Excede a capacidade máxima das salas ({{ cohortStore.maxRoomCapacity }} alunos).
  Considere criar uma turma adicional.
</p>
<p v-else class="text-xs text-gray-400 mt-1">
  Capacidade máxima das salas: {{ cohortStore.maxRoomCapacity }} alunos
</p>

          <div class="flex gap-3 pt-2">
            <button type="button" @click="showConfirmModal = false"
              class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition">
              Cancelar
            </button>
            <button @click="handleConfirm"
              class="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition flex items-center justify-center gap-2">
              <CheckCircle class="w-4 h-4" />
              Confirmar Ingressos
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Criar / Editar -->
    <div v-if="showModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="closeModal">
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
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
        </div>

        <div v-if="loadingDetail" class="flex items-center justify-center py-12">
          <Loader2 class="w-6 h-6 animate-spin text-blue-900" />
        </div>

        <form v-else @submit.prevent="handleSubmit" class="p-6 space-y-5">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Ano Curricular *</label>
              <input v-model.number="form.year" type="number" min="1" max="5" required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Secção *</label>
              <input v-model="form.section" type="text" required maxlength="2"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition uppercase" />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Ano Lectivo *</label>
              <input v-model.number="form.academicYear" type="number" required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Semestre *</label>
              <select v-model.number="form.semester" required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition">
                <option value="" disabled>Seleccionar</option>
                <option :value="1">1º Semestre</option>
                <option :value="2">2º Semestre</option>
              </select>
            </div>
          </div>

          <div v-if="!isEditing">
            <label class="block text-sm font-medium text-gray-700 mb-1">Curso *</label>
            <select v-model.number="form.courseId" required
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-900 focus:border-transparent transition">
              <option value="" disabled>Seleccionar curso</option>
              <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
                {{ course.name }}
              </option>
            </select>
          </div>

          <div class="flex gap-3 pt-2">
            <button type="button" @click="closeModal"
              class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition">
              Cancelar
            </button>
            <button type="submit"
              class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg hover:bg-blue-800 transition flex items-center justify-center gap-2">
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
import {
  Users, Plus, Edit, UserPlus, CheckCircle, Clock,
  AlertCircle, Trash2, Loader2, X
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

// Turma a confirmar
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

// ── Computed ────────────────────────────────────────────────────────
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
  ACTIVE: 'Activo',
  ARCHIVED: 'Arquivado',
}

function statusBadgeClass(status: string) {
  return {
    'ESTIMATED': 'bg-amber-100 text-amber-700',
    'CONFIRMED': 'bg-green-100 text-green-700',
    'ACTIVE':    'bg-blue-100 text-blue-700',
    'ARCHIVED':  'bg-gray-100 text-gray-500',
  }[status] ?? 'bg-gray-100 text-gray-500'
}

// ── Lifecycle ───────────────────────────────────────────────────────
onMounted(() => fetchCohorts())

async function fetchCohorts(page = 0) {
  currentPage.value = page
  await cohortStore.fetchCohorts(page, 10)
}

// ── Confirmar ingressos ─────────────────────────────────────────────
async function openConfirmModal(cohort: CohortListResponse & { turma: string }) {
  confirmingCohort.value = cohort
  confirmForm.studentCount = cohort.studentCount // pré-preenche com estimativa
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

// ── Modal criar/editar ──────────────────────────────────────────────
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
