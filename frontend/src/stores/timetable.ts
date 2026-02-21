import { defineStore } from 'pinia'
import { ref } from 'vue'
import { timetableService } from '@/services/timetableService'
import type { TimetableSolution } from '@/services/dto/timetable'

export const useTimetableStore = defineStore('timetable', () => {
  const solution = ref<TimetableSolution | null>(null)
  const loading = ref(false)
  const generating = ref(false)
  const currentJobId = ref<string | null>(null)
  const error = ref<string | null>(null)

  /**
   * Triggers generation and polls until the solver finishes.
   * Resolves when solution is available or rejects on error.
   */
  async function generate(
    academicYear: number,
    semester: number,
    onTick?: (attempt: number) => void,
  ) {
    generating.value = true
    error.value = null
    solution.value = null

    try {
      const { jobId } = await timetableService.generate(academicYear, semester)
      currentJobId.value = jobId
      await pollUntilReady(jobId, onTick)
    } catch (e: any) {
      error.value = e?.response?.data?.message ?? 'Erro ao gerar horário.'
      throw e
    } finally {
      generating.value = false
    }
  }

  /**
   * Polls GET /v1/solver/{jobId} every 3.5s until solution arrives or max attempts reached.
   */
  async function pollUntilReady(
    jobId: string,
    onTick?: (attempt: number) => void,
    maxAttempts = 20,
    intervalMs = 3500,
  ) {
    for (let i = 1; i <= maxAttempts; i++) {
      await new Promise(r => setTimeout(r, intervalMs))
      onTick?.(i)

      try {
        const result = await timetableService.getSolution(jobId)
        if (result !== null) {
          solution.value = result
          return
        }
      } catch {
        // 404 or network hiccup — keep trying until maxAttempts
      }
    }

    throw new Error('Tempo limite de geração excedido. Tente novamente.')
  }

  /**
   * Loads an existing solution by generating with the same params and
   * getting whatever is currently stored — or you can swap this for a
   * dedicated GET endpoint if you add one later.
   */
  async function loadSolution(jobId: string) {
    loading.value = true
    error.value = null
    try {
      const result = await timetableService.getSolution(jobId)
      solution.value = result
    } catch (e: any) {
      error.value = e?.response?.data?.message ?? 'Erro ao carregar horário.'
    } finally {
      loading.value = false
    }
  }

  function clear() {
    solution.value = null
    currentJobId.value = null
    error.value = null
  }

  return {
    solution,
    loading,
    generating,
    currentJobId,
    error,
    generate,
    loadSolution,
    clear,
  }
})
