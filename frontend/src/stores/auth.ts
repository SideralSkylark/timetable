// frontend/src/stores/auth.ts
import { defineStore } from 'pinia'
import authService from '@/services/authService'
import type { User } from '@/services/types/user'

interface AuthState {
  user: User | null;
  isAuthenticated: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: authService.getCurrentUser(),
    isAuthenticated: authService.isAuthenticated(),
  }),

  actions: {
    async login(email: string, password: string) {
      const data = await authService.login(email, password)
      this.user = data;
      this.isAuthenticated = true
    },

    logout() {
      authService.logout()
      this.user = null
      this.isAuthenticated = false
    },

    setUser(user: User | null) {
      this.user = user
      this.isAuthenticated = !!user
    }
  }
})
