import api from './api'
import type { ApiResponse } from './responses/apiResponse'
import type { Page } from './types/page'
import type {
  CohortListResponse,
  CohortResponse,
  ConfirmCohortRequest,
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

  async confirm(id: number, studentCount: number) {
    const res = await api.patch<ApiResponse<CohortResponse>>(
      `${BASE_URL}/${id}/confirm`,
      { studentCount }
    )
    return res.data.data
  },

  async getMaxRoomCapacity(): Promise<number> {
    const res = await api.get<ApiResponse<number>>('/v1/rooms/max-capacity')
    return res.data.data
  },

  async getByCourse(courseId: number, academicYear: number, semester: number) {
    const res = await api.get<ApiResponse<CohortListResponse[]>>(BASE_URL, {
      params: { courseId, academicYear, semester, size: 100 }
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

  async updateStudents(id: number, studentIds: number[]) {
    const res = await api.put<ApiResponse<CohortResponse>>(
      `${BASE_URL}/${id}/students`,
      { studentIds }
    )
    return res.data.data
  },

  async delete(id: number) {
    await api.delete(`${BASE_URL}/${id}`)
  },
}
