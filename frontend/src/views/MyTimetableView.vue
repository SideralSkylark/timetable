<template>
  <div class="min-h-screen bg-gray-50">

    <!-- Header -->
    <div class="mb-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
        <div class="flex items-center gap-3">
          <div class="bg-blue-900 p-2.5 rounded-lg">
            <CalendarDays class="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 class="text-xl font-semibold text-gray-900">O meu horário</h1>
            <p class="text-gray-400 text-sm">
              {{ isTeacher ? 'Aulas atribuídas' : 'Horário da turma' }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="mb-5">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 px-5 py-4">
        <div class="flex flex-wrap items-end gap-4">

          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Ano lectivo</label>
            <div class="relative">
              <select v-model="selectedYear"
                class="h-8 px-3 pr-8 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer">
                <option v-for="y in availableYears" :key="y" :value="y">{{ y }}</option>
              </select>
              <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Semestre</label>
            <div class="relative">
              <select v-model="selectedSemester"
                class="h-8 px-3 pr-8 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer">
                <option :value="1">1º semestre</option>
                <option :value="2">2º semestre</option>
              </select>
              <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <div v-if="hasLessons" class="flex-1 flex items-center justify-end gap-4">
            <div v-for="item in yearLegend" :key="item.year" class="flex items-center gap-1.5">
              <div class="w-2.5 h-2.5 rounded-sm" :class="item.dot"></div>
              <span class="text-xs text-gray-400">{{ item.label }}</span>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- Timetable grid -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">

      <div v-if="loading" class="flex flex-col items-center justify-center py-24 gap-3">
        <Loader2 class="w-7 h-7 animate-spin text-blue-900" />
        <p class="text-sm font-medium text-gray-600">A carregar horário...</p>
      </div>

      <div v-else-if="!hasLessons" class="flex flex-col items-center justify-center py-24 gap-3">
        <div class="bg-gray-100 p-4 rounded-full">
          <CalendarDays class="w-8 h-8 text-gray-400" />
        </div>
        <p class="text-sm font-semibold text-gray-600">Nenhum horário disponível</p>
        <p class="text-xs text-gray-400">
          {{ isTeacher ? 'Não tem aulas atribuídas para este período.' : 'Ainda não foi atribuído a uma turma ou o horário não foi publicado.' }}
        </p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full border-collapse">
          <thead>
            <tr>
              <th class="w-24 bg-gray-50 border-b border-r border-gray-100 px-3 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide text-center">Bloco</th>
              <th v-for="day in days" :key="day.value"
                class="bg-gray-50 border-b border-r border-gray-100 px-3 py-3 text-xs font-medium text-gray-500 uppercase tracking-wide text-center last:border-r-0">
                {{ day.label }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="block in blocks" :key="block.id" class="border-b border-gray-50 last:border-b-0">
              <td class="border-r border-gray-100 bg-gray-50 px-3 py-3 text-center align-middle">
                <div class="flex flex-col items-center gap-1">
                  <span class="text-xs font-mono font-semibold text-gray-600">{{ block.startTime }}</span>
                  <span class="text-xs font-medium px-2 py-0.5 rounded-full"
                    :class="block.period === 'MORNING' ? 'bg-amber-50 text-amber-600' : 'bg-blue-50 text-blue-600'">
                    {{ block.period === 'MORNING' ? 'Manhã' : 'Tarde' }}
                  </span>
                  <span class="text-xs font-mono text-gray-400">{{ block.endTime }}</span>
                </div>
              </td>
              <td v-for="day in days" :key="`${block.id}-${day.value}`"
                class="border-r border-gray-100 last:border-r-0 p-1.5 align-top"
                style="min-width: 130px; min-height: 76px;">
                <div v-for="lesson in getCellLessons(day.value, block.startTime)" :key="lesson.id"
                  class="rounded-md px-2 py-1.5 mb-1 text-xs"
                  :class="yearColorClass(lesson.cohort.year)">
                  <p class="font-semibold leading-tight line-clamp-2">{{ lesson.subject.name }}</p>
                  <p class="opacity-60 mt-0.5">{{ lesson.cohort.displayName }}</p>
                  <!-- Docente vê a sala; estudante vê o professor -->
                  <p v-if="isTeacher" class="opacity-50 mt-0.5">{{ lesson.room?.name }}</p>
                  <p v-else class="opacity-50 mt-0.5">{{ lesson.teacher?.fullName }}</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { timetableService } from '@/services/timetableService'
import type { LessonAssignment, TimetableSolution } from '@/services/dto/timetable'
import { CalendarDays, ChevronDown, Loader2 } from 'lucide-vue-next'

const auth = useAuthStore()

const isTeacher = computed(() => auth.user?.roles?.includes('TEACHER') ?? false)
const isStudent = computed(() => auth.user?.roles?.includes('STUDENT') ?? false)

const currentYear = new Date().getFullYear()
const availableYears = [currentYear - 1, currentYear, currentYear + 1]
const selectedYear = ref(currentYear)
const selectedSemester = ref(1)
const loading = ref(false)
const solution = ref<TimetableSolution | null>(null)

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
  { year: 1, label: '1º Ano', dot: 'bg-green-300' },
  { year: 2, label: '2º Ano', dot: 'bg-blue-300' },
  { year: 3, label: '3º Ano', dot: 'bg-orange-300' },
  { year: 4, label: '4º Ano', dot: 'bg-purple-300' },
  { year: 5, label: '5º Ano', dot: 'bg-pink-300' },
]

const lessons = computed(() => solution.value?.lessonAssignments ?? [])
const hasLessons = computed(() => lessons.value.length > 0)

function getCellLessons(day: string, blockStart: string): LessonAssignment[] {
  return lessons.value.filter(l =>
    l.timeslot?.dayOfWeek === day &&
    (l.timeslot?.startTime ?? '').substring(0, 5) === blockStart
  )
}

function yearColorClass(year: number): string {
  return ({
    1: 'bg-green-100 text-green-800 border border-green-200',
    2: 'bg-blue-100 text-blue-800 border border-blue-200',
    3: 'bg-orange-100 text-orange-800 border border-orange-200',
    4: 'bg-purple-100 text-purple-800 border border-purple-200',
    5: 'bg-pink-100 text-pink-800 border border-pink-200',
  } as Record<number, string>)[year] ?? 'bg-gray-100 text-gray-800 border border-gray-200'
}

async function load() {
  loading.value = true
  solution.value = null
  try {
    if (isTeacher.value) {
      solution.value = await timetableService.loadMyTeacherTimetable(selectedYear.value, selectedSemester.value)
    } else {
      solution.value = await timetableService.loadMyStudentTimetable(selectedYear.value, selectedSemester.value)
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch([selectedYear, selectedSemester], load)
</script>

<style scoped>
.line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>
