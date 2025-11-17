export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
    id: number;
    username: string;
    email: string;
    roles: [];
    updatedAt: string
}
