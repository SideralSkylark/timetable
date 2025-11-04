import api from './api';
import type { ApiResponse } from './responses/apiResponse';
import type { MessageResponse } from './responses/messageResponse';
import type { LoginRequest } from './dto/loginRequest';
import type { LoginResponse } from './dto/loginResponse';
import type { User } from './types/user';


class AuthService {
  async login(email: string, password: string): Promise<User> {

      const response = await api.post<ApiResponse<LoginResponse>>(
        '/v1/auth/login',
        { email, password } as LoginRequest
      );
      const user = {
        id: response.data.data.id,
        username: response.data.data.username,
        email: response.data.data.email,
        roles: response.data.data.roles
      }

      localStorage.setItem('user', JSON.stringify(user));
      return user;
  }

  async logout(): Promise<void> {
    try {
      await api.post<ApiResponse<void>>('/logout',{});
    } catch (error) {

    } finally {
      localStorage.removeItem('user');
    }
  }

  getCurrentUser(): User {
    const user = localStorage.getItem('user');
    const temp = user ? JSON.parse(user) : null;
    if (temp == null) {
        const emptyUser = {
            id: 0,
            username: "",
            email: "",
            roles: []
        }

        return emptyUser;
    }

    return temp;
  }

  isAuthenticated(): boolean {
    return !!this.getCurrentUser();
  }
}

export default new AuthService();
