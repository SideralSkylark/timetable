export interface CreateUserRequest {
  username: string
  email: string
  password: string
  roles: string[]
}

export interface UpdateUserRequest {
  username: string
  email: string
  roles: string[]
}

export interface UpdateUserProfileRequest {
  username: string
  email: string
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  roles: string[];
  enabled?: boolean;
}

