import { defineStore } from 'pinia'
import { cohortService } from '@/services/cohortService'
import type {
  CohortListResponse,
  CohortResponse,
  CreateCohortRequest,
  UpdateCohortRequest,
  CohortSummaryResponse,
} from '@/services/dto/cohort'
import type { Page } from '@/services/types/page'

export const useCohortStore = defineStore('cohorts', {
  state: () => ({
    cohortsPage: null as Page<CohortListResponse> | null,
    cohortSummary: null as CohortSummaryResponse | null,
    selectedCohort: null as CohortResponse | null,
    maxRoomCapacity: null as number | null,
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchCohorts(page = 0, size = 10, filters?: {
      name?: string
      courseId?: number
      academicYear?: number
      semester?: number
      status?: string
    }) {
      this.loading = true
      this.error = null
      try {
        const [pageData, summaryData] = await Promise.all([
          cohortService.getAll(page, size, filters),
          this.fetchCohortSummary(filters)
        ])
        this.cohortsPage = pageData
        this.cohortSummary = summaryData
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Erro ao carregar turmas'
      } finally {
        this.loading = false
      }
    },

    async fetchCohortSummary(filters?: {
      name?: string
      courseId?: number
      academicYear?: number
      semester?: number
    }) {
      try {
        const summary = await cohortService.getSummary(filters)
        this.cohortSummary = summary
        return summary
      } catch (err: any) {
        console.error('Failed to fetch cohort summary:', err)
        return null
      }
    },

    async fetchMaxRoomCapacity() {
      if (this.maxRoomCapacity !== null) return // cache — só busca uma vez
      try {
        this.maxRoomCapacity = await cohortService.getMaxRoomCapacity()
      } catch {
        this.maxRoomCapacity = 200 // fallback seguro
      }
    },

    async confirmCohort(id: number, studentCount: number) {
      this.loading = true
      this.error = null
      try {
        return await cohortService.confirm(id, studentCount)
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Erro ao confirmar ingressos'
        throw err
      } finally {
        this.loading = false
      }
    },

    async fetchCohort(id: number) {
      this.loading = true
      this.error = null
      try {
        this.selectedCohort = await cohortService.getById(id)
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Erro ao carregar turma'
      } finally {
        this.loading = false
      }
    },

    async createCohort(payload: CreateCohortRequest) {
      this.loading = true
      this.error = null
      try {
        return await cohortService.create(payload)
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Erro ao criar turma'
        throw err
      } finally {
        this.loading = false
      }
    },

    async updateCohort(id: number, payload: UpdateCohortRequest) {
      this.loading = true
      this.error = null
      try {
        return await cohortService.update(id, payload)
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Erro ao actualizar turma'
        throw err
      } finally {
        this.loading = false
      }
    },

    async updateCohortStudents(id: number, studentIds: number[]) {
      this.loading = true
      this.error = null
      try {
        return await cohortService.updateStudents(id, studentIds)
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Erro ao atribuir estudantes'
        throw err
      } finally {
        this.loading = false
      }
    },

    async deleteCohort(id: number) {
      this.loading = true
      this.error = null
      try {
        await cohortService.delete(id)
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Erro ao eliminar turma'
        throw err
      } finally {
        this.loading = false
      }
    },
  },
})
