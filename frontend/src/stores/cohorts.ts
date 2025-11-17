import { defineStore } from 'pinia'
import { cohortService } from '@/services/cohortService'
import type { CohortResponse, CreateCohortRequest, UpdateCohortRequest } from '@/services/dto/cohort'
import type { Page } from '@/services/types/page'

export const useCohortStore = defineStore('cohorts', {
  state: () => ({
    cohortsPage: null as Page<CohortResponse> | null,
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
        this.error = err.response?.data?.message || 'Failed to load cohorts'
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
        this.error = err.response?.data?.message || 'Failed to load cohort'
      } finally {
        this.loading = false
      }
    },

    async createCohort(payload: CreateCohortRequest) {
      this.loading = true
      this.error = null
      try {
        const cohort = await cohortService.create(payload)
        return cohort
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to create cohort'
        throw err
      } finally {
        this.loading = false
      }
    },

    async updateCohort(id: number, payload: UpdateCohortRequest) {
      this.loading = true
      this.error = null
      try {
        const cohort = await cohortService.update(id, payload)
        return cohort
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to update cohort'
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
        this.error = err.response?.data?.message || 'Failed to delete cohort'
        throw err
      } finally {
        this.loading = false
      }
    },
  },
})
