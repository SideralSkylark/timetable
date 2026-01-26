import api from './api'
import type { ApiResponse } from './responses/apiResponse'
import type { Page } from './types/page'

interface TeacherResponse {
  id: number
  name: string
  email: string
}

const BASE_URL = '/v1/teachers'

export const teacherService = {
  async getAll(page: number = 0, size: number = 10) {
    const res = await api.get<ApiResponse<Page<TeacherResponse>>>(BASE_URL, {
      params: { page, size },
    })
    return res.data.data
  },

  async getById(id: number) {
    const res = await api.get<ApiResponse<TeacherResponse>>(`${BASE_URL}/${id}`)
    return res.data.data
  },
}
