import { defineStore } from 'pinia'
import { authService, Role } from '@/services'
import type { UserResponse } from '@/services/dto/user'
import type { AxiosError } from 'axios'
import type { ErrorResponse } from '@/services/responses/errorResponse'

interface AuthState {
  user: UserResponse | null
  isAuthenticated: boolean
  loading: boolean
  error: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: authService.getCurrentUser(),
    isAuthenticated: authService.isAuthenticated(),
    loading: false,
    error: null,
  }),

  getters: {
    username: (state) => state.user?.username ?? '',
    roles: (state) => state.user?.roles ?? [],
    isAdmin: (state) => state.user?.roles.includes(Role.ADMIN) ?? false,
  },

  actions: {
    async login(email: string, password: string) {
      this.loading = true
      this.error = null

      try {
        const user = await authService.login(email, password)
        this.user = user
        this.isAuthenticated = true
      } catch (err) {
        const error = err as AxiosError<ErrorResponse>
        this.error = error.response?.data?.message || 'Erro ao autenticar.'
        throw err
      } finally {
        this.loading = false
      }
    },

    async register(username: string, email: string, password: string) {
      if (!this.isAdmin) {
        throw new Error('Não tem autorização para realizar esta ação.')
      }

      try {
        await authService.register(username, email, password)
      } catch (err) {
        const error = err as AxiosError<ErrorResponse>
        this.error = error.response?.data?.message || 'Erro ao registar utilizador.'
        throw err
      }
    },

    async logout() {
      try {
        await authService.logout()
      } finally {
        this.user = null
        this.isAuthenticated = false
      }
    },

    setUser(user: UserResponse | null) {
      this.user = user
      this.isAuthenticated = !!user
    },
  },
})
