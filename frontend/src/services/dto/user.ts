export type TeacherType = 'FULL_TIME' | 'PART_TIME'

export interface CreateUserRequest {
  username: string
  email: string
  password: string
  roles: string[]
  teacherType?: TeacherType
}

export interface UpdateUserRequest {
  username: string
  email: string
  roles: string[]
  teacherType?: TeacherType
}

export interface UpdateUserProfileRequest {
  username: string
  email: string
}

export interface UserResponse {
  id: number
  username: string
  email: string
  roles: string[]
  enabled?: boolean
  teacherType?: TeacherType
}
