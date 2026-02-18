export interface CourseListResponse {
  id: number
  name: string
  coordinatorId: number
  coordinatorName: string
  years: number
  subjectCount: number
  createdAt: string
  updatedAt: string
}

export interface CourseResponse {
  id: number
  name: string
  coordinatorId: number
  coordinatorName: string
  years: number
  createdAt: string
  updatedAt: string
}

export interface CoordinatorOption {
  id: number
  name: string
}

export interface CreateCourseRequest {
  name: string
  coordinatorId: number
  years: number
}

export interface UpdateCourseRequest {
  name: string
  coordinatorId: number
  years: number
}
