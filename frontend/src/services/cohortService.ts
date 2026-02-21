import api from './api'
import type { ApiResponse } from './responses/apiResponse'
import type { Page } from './types/page'
import type {
  CohortListResponse,
  CohortResponse,
  CreateCohortRequest,
  UpdateCohortRequest,
} from './dto/cohort'

const BASE_URL = '/v1/cohorts'

export const cohortService = {
  async getAll(page = 0, size = 10) {
    const res = await api.get<ApiResponse<Page<CohortListResponse>>>(BASE_URL, {
      params: { page, size },
    })
    return res.data.data
  },

  async getById(id: number) {
    const res = await api.get<ApiResponse<CohortResponse>>(`${BASE_URL}/${id}`)
    return res.data.data
  },

  async create(data: CreateCohortRequest) {
    const res = await api.post<ApiResponse<CohortResponse>>(BASE_URL, data)
    return res.data.data
  },

  async update(id: number, data: UpdateCohortRequest) {
    const res = await api.put<ApiResponse<CohortResponse>>(`${BASE_URL}/${id}`, data)
    return res.data.data
  },

  async delete(id: number) {
    await api.delete(`${BASE_URL}/${id}`)
  },
}
