<template>
  <div class="min-h-screen bg-gray-50 p-6">

    <!-- Header -->
    <div class="max-w-7xl mx-auto mb-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center gap-3">
          <div class="bg-blue-900 p-3 rounded-lg"><CalendarDays class="w-6 h-6 text-white" /></div>
          <div>
            <h1 class="text-2xl font-bold text-gray-900">Horários</h1>
            <p class="text-gray-500 text-sm">Visualizar e gerir horários da instituição</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters + Generate -->
    <div class="max-w-7xl mx-auto mb-6">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex flex-wrap items-end gap-4">

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Ano Lectivo</label>
            <div class="relative">
              <select v-model="selectedYear" class="px-4 py-2.5 pr-10 border border-gray-300 rounded-lg text-sm text-gray-900 bg-white appearance-none focus:outline-none focus:ring-2 focus:ring-blue-900 focus:border-transparent transition cursor-pointer">
                <option v-for="y in availableYears" :key="y" :value="y">{{ y }}</option>
              </select>
              <ChevronDown class="w-4 h-4 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Semestre</label>
            <div class="relative">
              <select v-model="selectedSemester" class="px-4 py-2.5 pr-10 border border-gray-300 rounded-lg text-sm text-gray-900 bg-white appearance-none focus:outline-none focus:ring-2 focus:ring-blue-900 focus:border-transparent transition cursor-pointer">
                <option :value="1">1º Semestre</option>
                <option :value="2">2º Semestre</option>
              </select>
              <ChevronDown class="w-4 h-4 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Turma</label>
            <div class="relative">
              <select v-model="selectedCohort" class="px-4 py-2.5 pr-10 border border-gray-300 rounded-lg text-sm text-gray-900 bg-white appearance-none focus:outline-none focus:ring-2 focus:ring-blue-900 focus:border-transparent transition cursor-pointer">
                <option value="">Todas as turmas</option>
                <option v-for="c in availableCohorts" :key="c.id" :value="c.id">{{ c.displayName }}</option>
              </select>
              <ChevronDown class="w-4 h-4 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <div class="flex-1" />

          <div v-if="timetableStore.loading" class="flex items-center gap-2 text-sm text-gray-500">
            <Loader2 class="w-4 h-4 animate-spin" />A carregar...
          </div>

          <div
            v-if="timetableStore.solution && !timetableStore.loading"
            class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium border"
            :class="timetableStore.solution.feasible ? 'bg-green-50 text-green-700 border-green-200' : 'bg-red-50 text-red-700 border-red-200'"
          >
            <CheckCircle v-if="timetableStore.solution.feasible" class="w-4 h-4" />
            <XCircle v-else class="w-4 h-4" />
            {{ timetableStore.solution.feasible ? 'Horário válido' : 'Com conflitos' }}
            <span class="font-mono text-xs opacity-60 ml-1">{{ timetableStore.solution.score }}</span>
          </div>

          <button
            v-if="isAdmin"
            :disabled="timetableStore.generating || timetableStore.loading"
            @click="showConfirmModal = true"
            class="flex items-center gap-2 px-5 py-2.5 bg-blue-900 text-white text-sm font-medium rounded-lg hover:bg-blue-800 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Loader2 v-if="timetableStore.generating" class="w-4 h-4 animate-spin" />
            <Zap v-else class="w-4 h-4" />
            {{ timetableStore.generating ? 'A gerar...' : 'Gerar Horário' }}
          </button>

        </div>
      </div>
    </div>

    <!-- Main area: grid + side panel -->
    <div class="max-w-7xl mx-auto flex gap-4 items-start">

      <!-- Timetable grid -->
      <div class="flex-1 bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">

        <div v-if="timetableStore.generating" class="flex flex-col items-center justify-center py-24 gap-3 text-gray-400">
          <Loader2 class="w-8 h-8 animate-spin text-blue-900" />
          <p class="text-sm font-medium text-gray-600">A gerar horário...</p>
          <p class="text-xs text-gray-400">Tentativa {{ pollAttempt }} de 20</p>
        </div>

        <div v-else-if="timetableStore.loading" class="flex flex-col items-center justify-center py-24 gap-3 text-gray-400">
          <Loader2 class="w-8 h-8 animate-spin text-blue-900" />
          <p class="text-sm font-medium text-gray-600">A carregar horário...</p>
        </div>

        <div v-else-if="!hasLessons" class="flex flex-col items-center justify-center py-24 gap-3">
          <div class="bg-gray-100 p-5 rounded-full"><CalendarDays class="w-10 h-10 text-gray-400" /></div>
          <p class="text-base font-semibold text-gray-700">Nenhum horário gerado</p>
          <p class="text-sm text-gray-500">
            {{ isAdmin ? 'Seleccione o ano e semestre e clique em "Gerar Horário".' : 'Aguarde a geração pelo administrador.' }}
          </p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full border-collapse">
            <thead>
              <tr>
                <th class="w-28 bg-gray-50 border-b border-r border-gray-200 px-3 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider text-center">Bloco</th>
                <th v-for="day in days" :key="day.value" class="bg-gray-50 border-b border-r border-gray-200 px-3 py-3 text-xs font-semibold text-gray-600 uppercase tracking-wider text-center last:border-r-0">
                  {{ day.label }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="block in blocks" :key="block.id" class="border-b border-gray-100 last:border-b-0">
                <td class="border-r border-gray-200 bg-gray-50 px-3 py-3 text-center align-middle">
                  <div class="flex flex-col items-center gap-1">
                    <span class="text-xs font-mono font-semibold text-gray-700">{{ block.startTime }}</span>
                    <span class="text-xs font-semibold px-2 py-0.5 rounded-full" :class="block.period === 'MORNING' ? 'bg-amber-100 text-amber-700' : 'bg-blue-100 text-blue-700'">
                      {{ block.period === 'MORNING' ? 'Manhã' : 'Tarde' }}
                    </span>
                    <span class="text-xs font-mono text-gray-400">{{ block.endTime }}</span>
                  </div>
                </td>
                <td
                  v-for="day in days"
                  :key="`${block.id}-${day.value}`"
                  class="border-r border-gray-100 last:border-r-0 p-1.5 align-top transition-colors"
                  :class="isValidSwapTarget(day.value, block.startTime) ? 'bg-green-50 ring-1 ring-inset ring-green-300 cursor-pointer' : ''"
                  style="min-width: 140px; min-height: 80px;"
                  @click="isValidSwapTarget(day.value, block.startTime) && confirmSwap(day.value, block.startTime)"
                >
                  <!-- Valid swap target indicator -->
                  <div
                    v-if="isValidSwapTarget(day.value, block.startTime) && getCellLessons(day.value, block.startTime).length === 0"
                    class="h-full flex items-center justify-center"
                  >
                    <div class="w-8 h-8 rounded-full bg-green-200 flex items-center justify-center">
                      <ArrowRightLeft class="w-4 h-4 text-green-700" />
                    </div>
                  </div>

                  <!-- Lessons -->
                  <div
                    v-for="lesson in getCellLessons(day.value, block.startTime)"
                    :key="lesson.id"
                    class="rounded-md px-2 py-1.5 mb-1 transition-all"
                    :class="[
                      yearColorClass(lesson.cohort.year),
                      selectedLesson?.id === lesson.id
                        ? 'ring-2 ring-blue-500 shadow-md scale-[1.02]'
                        : canSelectLesson ? 'cursor-pointer hover:shadow-md hover:scale-[1.02]' : 'cursor-default hover:shadow-md hover:scale-[1.02]',
                    ]"
                    @click.stop="canSelectLesson && selectLesson(lesson)"
                    @mouseenter="!selectedLesson && (hoveredLesson = lesson)"
                    @mouseleave="!selectedLesson && (hoveredLesson = null)"
                  >
                    <p class="text-xs font-semibold leading-tight line-clamp-2">{{ lesson.subject.name }}</p>
                    <p class="text-xs opacity-70 mt-0.5">{{ lesson.cohort.displayName }}</p>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Permutation side panel — only when viewing a single cohort and admin -->
      <Transition name="slide-panel">
        <div
          v-if="isAdmin && selectedCohort && selectedLesson"
          class="w-72 bg-white rounded-lg shadow-sm border border-gray-200 flex-shrink-0"
        >
          <div class="p-4 border-b border-gray-200 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <ArrowRightLeft class="w-4 h-4 text-blue-900" />
              <h3 class="text-sm font-semibold text-gray-900">Permutar Aula</h3>
            </div>
            <button @click="clearSelection" class="text-gray-400 hover:text-gray-600 transition">
              <X class="w-4 h-4" />
            </button>
          </div>

          <div class="p-4 space-y-3">
            <!-- Selected lesson info -->
            <div class="bg-gray-50 rounded-lg p-3 text-xs space-y-1">
              <p class="font-semibold text-gray-800 text-sm">{{ selectedLesson.subject.name }}</p>
              <p class="text-gray-500">{{ selectedLesson.cohort.displayName }}</p>
              <p class="text-gray-500">
                {{ dayLabel(selectedLesson.timeslot?.dayOfWeek) }}
                · {{ selectedLesson.timeslot?.startTime?.substring(0, 5) }}
              </p>
              <p class="text-gray-500">{{ selectedLesson.room?.name }}</p>
            </div>

            <!-- Calculate button -->
            <button
              :disabled="loadingSlots"
              @click="calculateValidSlots"
              class="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-blue-900 text-white text-sm font-medium rounded-lg hover:bg-blue-800 transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Loader2 v-if="loadingSlots" class="w-4 h-4 animate-spin" />
              <ArrowRightLeft v-else class="w-4 h-4" />
              {{ loadingSlots ? 'A calcular...' : validSlots.length > 0 ? 'Recalcular' : 'Calcular Permutações' }}
            </button>

            <!-- Result -->
            <div v-if="validSlots.length > 0" class="text-xs text-gray-500 text-center">
              {{ validSlots.length }} slot{{ validSlots.length !== 1 ? 's' : '' }} disponíve{{ validSlots.length !== 1 ? 'is' : 'l' }} — clique numa célula verde
            </div>
            <div v-else-if="slotsCalculated && validSlots.length === 0" class="text-xs text-amber-600 bg-amber-50 rounded-lg p-3 text-center">
              Nenhum slot válido encontrado para esta aula.
            </div>

            <button
              @click="clearSelection"
              class="w-full px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
            >
              Cancelar
            </button>
          </div>
        </div>
      </Transition>

    </div>

    <!-- Legend -->
    <div v-if="hasLessons" class="max-w-7xl mx-auto mt-3 flex flex-wrap gap-4 px-1">
      <div v-for="item in yearLegend" :key="item.year" class="flex items-center gap-1.5">
        <div class="w-3 h-3 rounded-sm" :class="item.dot"></div>
        <span class="text-xs text-gray-500">{{ item.label }}</span>
      </div>
      <div v-if="isAdmin && selectedCohort" class="flex items-center gap-1.5 ml-4">
        <div class="w-3 h-3 rounded-sm bg-green-200"></div>
        <span class="text-xs text-gray-500">Slot disponível para permutação</span>
      </div>
    </div>

    <!-- Tooltip (only when no lesson selected) -->
    <Teleport to="body">
      <Transition name="tooltip">
        <div v-if="hoveredLesson && !selectedLesson" class="fixed bottom-6 right-6 bg-gray-900 text-white rounded-xl shadow-2xl p-4 w-60 z-50 pointer-events-none">
          <p class="font-semibold text-sm mb-3 pb-2 border-b border-white/10 leading-snug">{{ hoveredLesson.subject.name }}</p>
          <div class="space-y-1.5">
            <div class="flex justify-between text-xs gap-3"><span class="text-gray-400 shrink-0">Turma</span><span class="text-right">{{ hoveredLesson.cohort.displayName }}</span></div>
            <div class="flex justify-between text-xs gap-3"><span class="text-gray-400 shrink-0">Professor</span><span class="text-right">{{ hoveredLesson.teacher?.fullName ?? '—' }}</span></div>
            <div class="flex justify-between text-xs gap-3"><span class="text-gray-400 shrink-0">Sala</span><span class="text-right">{{ hoveredLesson.room?.name ?? '—' }}</span></div>
            <div class="flex justify-between text-xs gap-3"><span class="text-gray-400 shrink-0">Créditos</span><span>{{ hoveredLesson.subject.credits }}</span></div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Confirm generate modal -->
    <div v-if="showConfirmModal" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50" @click.self="showConfirmModal = false">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-sm">
        <div class="border-b border-gray-200 p-6">
          <div class="flex items-center gap-3">
            <div class="bg-blue-100 p-2 rounded-lg"><Zap class="w-5 h-5 text-blue-900" /></div>
            <h2 class="text-lg font-semibold text-gray-900">Gerar Horário</h2>
          </div>
          <p class="text-sm text-gray-500 mt-3 leading-relaxed">
            Vai gerar o horário para <strong class="text-gray-700">{{ selectedYear }} · {{ selectedSemester }}º Semestre</strong>.
            <template v-if="timetableStore.solution">
              <br /><span class="text-amber-600 font-medium">O horário existente será substituído.</span>
            </template>
            A operação pode demorar até 60 segundos.
          </p>
        </div>
        <div class="px-6 py-4 bg-gray-50 flex justify-end gap-3 rounded-b-lg">
          <button @click="showConfirmModal = false" class="px-4 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition">Cancelar</button>
          <button @click="handleGenerate" class="px-5 py-2.5 text-sm font-medium text-white bg-blue-900 rounded-lg hover:bg-blue-800 transition flex items-center gap-2">
            <Zap class="w-4 h-4" />Confirmar
          </button>
        </div>
      </div>
    </div>

    <!-- Confirm swap modal -->
    <div v-if="pendingSwap" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50" @click.self="pendingSwap = null">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-sm">
        <div class="border-b border-gray-200 p-6">
          <div class="flex items-center gap-3">
            <div class="bg-green-100 p-2 rounded-lg"><ArrowRightLeft class="w-5 h-5 text-green-700" /></div>
            <h2 class="text-lg font-semibold text-gray-900">Confirmar Permutação</h2>
          </div>
          <div class="text-sm text-gray-500 mt-3 space-y-2">
            <p><strong class="text-gray-700">{{ selectedLesson?.subject.name }}</strong></p>
            <div class="flex items-center gap-3 text-xs">
              <span class="bg-gray-100 px-2 py-1 rounded">{{ dayLabel(selectedLesson?.timeslot?.dayOfWeek) }} · {{ selectedLesson?.timeslot?.startTime?.substring(0, 5) }}</span>
              <ArrowRightLeft class="w-3 h-3 text-gray-400" />
              <span class="bg-green-100 px-2 py-1 rounded text-green-700">{{ dayLabel(pendingSwap?.dayOfWeek) }} · {{ pendingSwap?.startTime?.substring(0, 5) }}</span>
            </div>
          </div>
        </div>
        <div class="px-6 py-4 bg-gray-50 flex justify-end gap-3 rounded-b-lg">
          <button @click="pendingSwap = null" class="px-4 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition">Cancelar</button>
          <button @click="handleApplySwap" :disabled="applyingSwap" class="px-5 py-2.5 text-sm font-medium text-white bg-green-700 rounded-lg hover:bg-green-600 transition flex items-center gap-2 disabled:opacity-50">
            <Loader2 v-if="applyingSwap" class="w-4 h-4 animate-spin" />
            <ArrowRightLeft v-else class="w-4 h-4" />
            Confirmar
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useTimetableStore } from '@/stores/timetable'
import { useToast } from '@/composables/useToast'
import { permutationService, type ValidSlot } from '@/services/permutationService'
import type { LessonAssignment } from '@/services/dto/timetable'
import { CalendarDays, ChevronDown, Zap, Loader2, CheckCircle, XCircle, ArrowRightLeft, X } from 'lucide-vue-next'

const auth = useAuthStore()
const timetableStore = useTimetableStore()
const toast = useToast()

const isAdmin = computed(() => auth.user?.roles?.includes('ADMIN') ?? false)

// ── Filters ────────────────────────────────────────────────────────
const currentYear = new Date().getFullYear()
const availableYears = [currentYear - 1, currentYear, currentYear + 1]
const selectedYear = ref(currentYear)
const selectedSemester = ref(1)
const selectedCohort = ref<number | ''>('')
const showConfirmModal = ref(false)
const hoveredLesson = ref<LessonAssignment | null>(null)
const pollAttempt = ref(0)

// ── Permutation state ──────────────────────────────────────────────
const selectedLesson = ref<LessonAssignment | null>(null)
const validSlots = ref<ValidSlot[]>([])
const slotsCalculated = ref(false)
const loadingSlots = ref(false)
const pendingSwap = ref<ValidSlot | null>(null)
const applyingSwap = ref(false)

// Can only select a lesson when viewing a single cohort and admin
const canSelectLesson = computed(() => isAdmin.value && !!selectedCohort.value)

// ── Load on mount and period change ───────────────────────────────
onMounted(() => timetableStore.loadForPeriod(selectedYear.value, selectedSemester.value))

watch([selectedYear, selectedSemester], ([year, semester]) => {
  selectedCohort.value = ''
  clearSelection()
  timetableStore.loadForPeriod(year, semester)
})

// Clear permutation state when cohort filter changes
watch(selectedCohort, () => clearSelection())

// ── Static ─────────────────────────────────────────────────────────
const days = [
  { value: 'MONDAY', label: 'Segunda' }, { value: 'TUESDAY', label: 'Terça' },
  { value: 'WEDNESDAY', label: 'Quarta' }, { value: 'THURSDAY', label: 'Quinta' },
  { value: 'FRIDAY', label: 'Sexta' },
]
const blocks = [
  { id: 1, startTime: '07:00', endTime: '08:45', period: 'MORNING' },
  { id: 2, startTime: '08:50', endTime: '10:35', period: 'MORNING' },
  { id: 3, startTime: '10:40', endTime: '12:25', period: 'MORNING' },
  { id: 4, startTime: '12:30', endTime: '14:15', period: 'AFTERNOON' },
  { id: 5, startTime: '14:20', endTime: '16:05', period: 'AFTERNOON' },
  { id: 6, startTime: '16:10', endTime: '17:55', period: 'AFTERNOON' },
]
const yearLegend = [
  { year: 1, label: '1º Ano', dot: 'bg-green-300' }, { year: 2, label: '2º Ano', dot: 'bg-blue-300' },
  { year: 3, label: '3º Ano', dot: 'bg-orange-300' }, { year: 4, label: '4º Ano', dot: 'bg-purple-300' },
  { year: 5, label: '5º Ano', dot: 'bg-pink-300' },
]

// ── Computed ───────────────────────────────────────────────────────
const allLessons = computed(() => timetableStore.solution?.lessonAssignments ?? [])

const availableCohorts = computed(() => {
  const seen = new Set<number>()
  const result: { id: number; displayName: string }[] = []
  for (const l of allLessons.value) {
    if (!seen.has(l.cohort.id)) { seen.add(l.cohort.id); result.push(l.cohort) }
  }
  return result.sort((a, b) => a.displayName.localeCompare(b.displayName))
})

const filteredLessons = computed(() =>
  selectedCohort.value ? allLessons.value.filter(l => l.cohort.id === selectedCohort.value) : allLessons.value
)

const hasLessons = computed(() => filteredLessons.value.length > 0)

// Set of "dayOfWeek|startTime" strings for valid swap targets
const validSlotKeys = computed(() =>
  new Set(validSlots.value.map(s => `${s.dayOfWeek}|${s.startTime.substring(0, 5)}`))
)

// ── Helpers ────────────────────────────────────────────────────────
function getCellLessons(day: string, blockStart: string) {
  return filteredLessons.value.filter(l => {
    const slotStart = (l.timeslot?.startTime ?? '').substring(0, 5)
    return l.timeslot?.dayOfWeek === day && slotStart === blockStart
  })
}

function isValidSwapTarget(day: string, blockStart: string): boolean {
  return validSlotKeys.value.has(`${day}|${blockStart}`)
}

function yearColorClass(year: number): string {
  const map: Record<number, string> = {
    1: 'bg-green-100 text-green-800 border border-green-200',
    2: 'bg-blue-100 text-blue-800 border border-blue-200',
    3: 'bg-orange-100 text-orange-800 border border-orange-200',
    4: 'bg-purple-100 text-purple-800 border border-purple-200',
    5: 'bg-pink-100 text-pink-800 border border-pink-200',
  }
  return map[year] ?? 'bg-gray-100 text-gray-800 border border-gray-200'
}

function dayLabel(day?: string): string {
  return days.find(d => d.value === day)?.label ?? day ?? ''
}

// ── Permutation actions ────────────────────────────────────────────
function selectLesson(lesson: LessonAssignment) {
  if (selectedLesson.value?.id === lesson.id) {
    clearSelection()
    return
  }
  selectedLesson.value = lesson
  validSlots.value = []
  slotsCalculated.value = false
}

function clearSelection() {
  selectedLesson.value = null
  validSlots.value = []
  slotsCalculated.value = false
  hoveredLesson.value = null
}

async function calculateValidSlots() {
  if (!selectedLesson.value) return
  loadingSlots.value = true
  slotsCalculated.value = false
  try {
    validSlots.value = await permutationService.getValidSlots(
      selectedLesson.value.id,
      selectedYear.value,
      selectedSemester.value,
    )
    slotsCalculated.value = true
    if (validSlots.value.length === 0) {
      toast.info('Nenhum slot válido encontrado para esta aula.')
    }
  } catch {
    toast.error('Erro ao calcular permutações.')
  } finally {
    loadingSlots.value = false
  }
}

function confirmSwap(day: string, blockStart: string) {
  const slot = validSlots.value.find(
    s => s.dayOfWeek === day && s.startTime.substring(0, 5) === blockStart
  )
  if (slot) pendingSwap.value = slot
}

async function handleApplySwap() {
  if (!selectedLesson.value || !pendingSwap.value) return
  applyingSwap.value = true
  try {
    await permutationService.applySwap(selectedLesson.value.id, pendingSwap.value.timeslotId)
    toast.success('Permutação aplicada com sucesso!')
    pendingSwap.value = null
    clearSelection()
    // Reload timetable to reflect the change
    await timetableStore.loadForPeriod(selectedYear.value, selectedSemester.value)
  } catch {
    toast.error('Erro ao aplicar permutação.')
  } finally {
    applyingSwap.value = false
  }
}

// ── Generate ───────────────────────────────────────────────────────
async function handleGenerate() {
  showConfirmModal.value = false
  selectedCohort.value = ''
  clearSelection()
  pollAttempt.value = 0
  toast.info('A gerar horário, pode demorar até 60 segundos...')
  try {
    await timetableStore.generate(selectedYear.value, selectedSemester.value, (attempt) => { pollAttempt.value = attempt })
    toast.success('Horário gerado com sucesso!')
  } catch {
    toast.error(timetableStore.error ?? 'Erro ao gerar horário.')
  }
}
</script>

<style scoped>
.tooltip-enter-active, .tooltip-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.tooltip-enter-from, .tooltip-leave-to { opacity: 0; transform: translateY(6px); }
.slide-panel-enter-active, .slide-panel-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.slide-panel-enter-from, .slide-panel-leave-to { opacity: 0; transform: translateX(16px); }
.line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>
