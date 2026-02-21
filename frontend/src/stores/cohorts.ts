import { defineStore } from 'pinia'
import { cohortService } from '@/services/cohortService'
import type {
  CohortListResponse,
  CohortResponse,
  CreateCohortRequest,
  UpdateCohortRequest,
} from '@/services/dto/cohort'
import type { Page } from '@/services/types/page'

export const useCohortStore = defineStore('cohorts', {
  state: () => ({
    cohortsPage: null as Page<CohortListResponse> | null,
    selectedCohort: null as CohortResponse | null,
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchCohorts(page = 0, size = 10) {
      this.loading = true
      this.error = null
      try {
        this.cohortsPage = await cohortService.getAll(page, size)
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Erro ao carregar turmas'
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
