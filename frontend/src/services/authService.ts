import api from './api'
import type { ApiResponse } from './responses/apiResponse'
import type { LoginRequest } from './dto/loginRequest'
import type { LoginResponse } from './dto/loginResponse'
import type { RegisterRequest } from './dto/registerRequest'
import type { RegisterResponse } from './dto/registerResponse'
import type { User } from './types/user'

const USER_KEY = 'user'

class AuthService {
  async login(email: string, password: string): Promise<User> {
    const { data } = await api.post<ApiResponse<LoginResponse>>(
      '/v1/auth/login',
      { email, password } as LoginRequest
    )

    const user: User = {
      id: data.data.id,
      username: data.data.username,
      email: data.data.email,
      roles: data.data.roles,
    }

    localStorage.setItem(USER_KEY, JSON.stringify(user))
    return user
  }

  async register(username: string, email: string, password: string): Promise<RegisterResponse> {
    const { data } = await api.post<ApiResponse<RegisterResponse>>(
      '/v1/auth/register',
      { username, email, password } as RegisterRequest
    )
    return data.data
  }

  async logout(): Promise<void> {
    try {
      await api.post('/v1/auth/logout', {})
    } finally {
      localStorage.removeItem(USER_KEY)
    }
  }

  getCurrentUser(): User | null {
    const stored = localStorage.getItem(USER_KEY)
    return stored ? JSON.parse(stored) : null
  }

  isAuthenticated(): boolean {
    return !!this.getCurrentUser()
  }
}

export default new AuthService()
