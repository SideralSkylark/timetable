import api from './api'
import type { ApiResponse } from './responses/apiResponse'
import type { Page } from './types/page'
import type {
  CreateCourseRequest,
  UpdateCourseRequest,
  CourseResponse,
  CourseListResponse,
  CoordinatorOption,
} from './dto/course'

const BASE_URL = '/v1/courses'

export const courseService = {
  async create(data: CreateCourseRequest) {
    const res = await api.post<ApiResponse<CourseResponse>>(BASE_URL, data)
    return res.data.data
  },

  async getAll(page = 0, size = 10) {
    const res = await api.get<ApiResponse<Page<CourseListResponse>>>(BASE_URL, {
      params: { page, size },
    })
    return res.data.data
  },

  async getById(id: number) {
    const res = await api.get<ApiResponse<CourseResponse>>(`${BASE_URL}/${id}`)
    return res.data.data
  },

  async getCoordinators(page = 0, size = 20) {
    const res = await api.get<ApiResponse<Page<CoordinatorOption>>>(
      `${BASE_URL}/coordinators`,
      { params: { page, size } },
    )
    return res.data.data
  },

  async update(id: number, data: UpdateCourseRequest) {
    const res = await api.put<ApiResponse<CourseResponse>>(`${BASE_URL}/${id}`, data)
    return res.data.data
  },

  async delete(id: number) {
    await api.delete(`${BASE_URL}/${id}`)
  },
}
