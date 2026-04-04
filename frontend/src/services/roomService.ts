import api from './api'
import type { ApiResponse } from './responses/apiResponse'
import type { Page } from './types/page'
import type {
  CreateRoomRequest,
  UpdateRoomRequest,
  RoomResponse,
} from './dto/room'

const BASE_URL = '/v1/rooms'

export const roomService = {
  async create(data: CreateRoomRequest) {
    const res = await api.post<ApiResponse<RoomResponse>>(BASE_URL, data)
    return res.data.data
  },

  async getAll(page: number = 0, size: number = 10, filters?: { 
    name?: string; 
    capacityMin?: number; 
    capacityMax?: number; 
    courseId?: number; 
    period?: string 
  }) {
    const params: any = { page, size }
    if (filters) {
      if (filters.name) params.name = filters.name
      if (filters.capacityMin !== undefined && filters.capacityMin !== null) params.capacityMin = filters.capacityMin
      if (filters.capacityMax !== undefined && filters.capacityMax !== null) params.capacityMax = filters.capacityMax
      if (filters.courseId) params.courseId = filters.courseId
      if (filters.period) params.period = filters.period
    }

    const res = await api.get<ApiResponse<Page<RoomResponse>>>(BASE_URL, {
      params,
    })
    return res.data.data
  },

  async getById(id: number) {
    const res = await api.get<ApiResponse<RoomResponse>>(`${BASE_URL}/${id}`)
    return res.data.data
  },

  async update(id: number, data: UpdateRoomRequest) {
    const res = await api.put<ApiResponse<RoomResponse>>(
      `${BASE_URL}/${id}`,
      data
    )
    return res.data.data
  },

  async delete(id: number) {
    await api.delete(`${BASE_URL}/${id}`)
  },
}

