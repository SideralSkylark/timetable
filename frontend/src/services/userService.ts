import api from './api'
import type { ApiResponse } from './responses/apiResponse'
import type { Page } from './types/page'
import type {
  CreateUserRequest,
  UpdateUserRequest,
  UpdateUserProfileRequest,
  UserResponse,
} from './dto/user'

export const userService = {
  me: {
    getProfile: async () => {
      const res = await api.get<ApiResponse<UserResponse>>('/v1/users/me')
      return res.data.data
    },
    updateProfile: async (data: UpdateUserProfileRequest) => {
      const res = await api.put<ApiResponse<UserResponse>>('/v1/users/me', data)
      return res.data.data
    },
    deleteProfile: async () => {
      await api.delete('/v1/users/me')
    },
  },
  admin: {
    create: async (data: CreateUserRequest) => {
      const res = await api.post<ApiResponse<UserResponse>>('/v1/admins', data)
      return res.data.data
    },
    getAll: async (page = 0, size = 10) => {
      const res = await api.get<ApiResponse<Page<UserResponse>>>('/v1/admins', {
        params: { page, size },
      })
      return res.data.data
    },
    getById: async (id: number) => {
      const res = await api.get<ApiResponse<UserResponse>>(`/v1/admins/${id}`)
      return res.data.data
    },
    update: async (id: number, data: UpdateUserRequest) => {
      const res = await api.put<ApiResponse<UserResponse>>(`/v1/admins/${id}`, data)
      return res.data.data
    },
    delete: async (id: number) => {
      await api.delete(`/v1/admins/${id}`)
    }, 
  },
}

