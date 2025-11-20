import { defineStore } from 'pinia'
import { userService } from '@/services/userService'
import type { UserResponse, CreateUserRequest, UpdateUserProfileRequest, UpdateUserRequest } from '@/services/dto/user'
import type { Page } from '@/services/types/page'

interface State {
  users: UserResponse[]
  pagedUsers: Page<UserResponse> | null
  currentUser: UserResponse | null
  loading: boolean
  error: string | null
}

export const useUserStore = defineStore('user', {
  state: (): State => ({
    users: [],
    pagedUsers: null,
    currentUser: null,
    loading: false,
    error: null,
  }),

  actions: {
    // ===============================
    // Authenticated user actions
    // ===============================
    async fetchCurrentUser() {
      this.loading = true
      try {
        this.currentUser = await userService.me.getProfile()
        this.error = null
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch profile'
      } finally {
        this.loading = false
      }
    },

    async updateProfile(data: UpdateUserProfileRequest) {
      this.loading = true
      try {
        this.currentUser = await userService.me.updateProfile(data)
        this.error = null
      } catch (err: any) {
        this.error = err.message || 'Failed to update profile'
      } finally {
        this.loading = false
      }
    },

    async deleteProfile() {
      this.loading = true
      try {
        await userService.me.deleteProfile()
        this.currentUser = null
        this.error = null
      } catch (err: any) {
        this.error = err.message || 'Failed to delete profile'
      } finally {
        this.loading = false
      }
    },

    // ===============================
    // Admin actions
    // ===============================
    async fetchUsers(page = 0, size = 20) {
      this.loading = true
      try {
        const paged = await userService.admin.getAll(page, size)
        this.pagedUsers = paged
        this.users = paged.content
        this.error = null
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch users'
      } finally {
        this.loading = false
      }
    },

    async createUser(data: CreateUserRequest) {
      this.loading = true
      try {
        const newUser = await userService.admin.create(data)
        this.users.push(newUser)
        this.error = null
        return newUser
      } catch (err: any) {
        this.error = err.message || 'Failed to create user'
        throw err
      } finally {
        this.loading = false
      }
    },

    async updateUser(id: number, data: UpdateUserRequest) {
      this.loading = true
      try {
        const updated = await userService.admin.update(id, data)
        const index = this.users.findIndex(u => u.id === id)
        if (index !== -1) this.users[index] = updated
        this.error = null
        return updated
      } catch (err: any) {
        this.error = err.message || 'Failed to update user'
        throw err
      } finally {
        this.loading = false
      }
    },

    async deleteUser(id: number) {
      this.loading = true
      try {
        await userService.admin.delete(id)
        this.users = this.users.filter(u => u.id !== id)
        this.error = null
      } catch (err: any) {
        this.error = err.message || 'Failed to delete user'
        throw err
      } finally {
        this.loading = false
      }
    },
  },
})

