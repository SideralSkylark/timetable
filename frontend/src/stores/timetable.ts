import { defineStore } from 'pinia'
import { ref } from 'vue'
import { timetableService } from '@/services/timetableService'
import type { TimetableSolution } from '@/services/dto/timetable'

export const useTimetableStore = defineStore('timetable', () => {
  const solution = ref<TimetableSolution | null>(null)
  const loading = ref(false)
  const generating = ref(false)
  const error = ref<string | null>(null)

  /** Loads persisted timetable from DB. Called on mount and on period filter change. */
  async function loadForPeriod(academicYear: number, semester: number) {
    loading.value = true
    error.value = null
    try {
      solution.value = await timetableService.loadPersisted(academicYear, semester)
    } catch (e: any) {
      error.value = e?.response?.data?.message ?? 'Erro ao carregar horário.'
      solution.value = null
    } finally {
      loading.value = false
    }
  }

  /** Triggers generation, polls until done, then reloads from DB. */
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
      await pollUntilReady(jobId, onTick)
      solution.value = await timetableService.loadPersisted(academicYear, semester)
    } catch (e: any) {
      error.value = e?.response?.data?.message ?? 'Erro ao gerar horário.'
      throw e
    } finally {
      generating.value = false
    }
  }

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
        if (result !== null) return
      } catch { /* keep polling */ }
    }
    throw new Error('Tempo limite de geração excedido. Tente novamente.')
  }

  function clear() {
    solution.value = null
    error.value = null
  }

  async function submitForApproval() {
    if (!solution.value) return
    await timetableService.submitForApproval(solution.value.id)
    solution.value = { ...solution.value, status: 'PENDING_APPROVAL' }
  }

  async function approve() {
    if (!solution.value) return
    await timetableService.approve(solution.value.id)
    solution.value = { ...solution.value, status: 'APPROVED' }
  }

  async function reject() {
    if (!solution.value) return
    await timetableService.reject(solution.value.id)
    solution.value = { ...solution.value, status: 'DRAFT' }
  }

  async function publish() {
    if (!solution.value) return
    await timetableService.publish(solution.value.id)
    solution.value = { ...solution.value, status: 'PUBLISHED' }
  }

  return { solution, loading, generating, error, loadForPeriod, generate, clear, submitForApproval, approve, reject, publish }
})
