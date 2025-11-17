import api from './api'
import type { ApiResponse } from './responses/apiResponse'
import type { Page } from './types/page'
import type {
  CreateCohortRequest,
  UpdateCohortRequest,
  CohortResponse,
} from './dto/cohort'

const BASE_URL = '/v1/cohorts'

export const cohortService = {
  async create(data: CreateCohortRequest) {
    const res = await api.post<ApiResponse<CohortResponse>>(BASE_URL, data)
    return res.data.data
  },

  async getAll(page: number = 0, size: number = 10) {
    const res = await api.get<ApiResponse<Page<CohortResponse>>>(BASE_URL, {
      params: { page, size },
    })
    return res.data.data
  },

  async getById(id: number) {
    const res = await api.get<ApiResponse<CohortResponse>>(`${BASE_URL}/${id}`)
    return res.data.data
  },

  async update(id: number, data: UpdateCohortRequest) {
    const res = await api.put<ApiResponse<CohortResponse>>(
      `${BASE_URL}/${id}`,
      data
    )
    return res.data.data
  },

  async delete(id: number) {
    await api.delete(`${BASE_URL}/${id}`)
  },
}
