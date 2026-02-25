export interface CourseListResponse {
  id: number
  name: string
  coordinatorId: number
  coordinatorName: string
  years: number
  expectedCohortsPerYear: Record<number, number>
  hasBusinessSimulation: boolean
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
  expectedCohortsPerYear: Record<number, number>
  hasBusinessSimulation: boolean   // ← NOVO
  createdAt: string
  updatedAt: string
}

export interface CreateCourseRequest {
  name: string
  coordinatorId: number
  years: number
  expectedCohortsPerYear: Record<number, number>
  hasBusinessSimulation: boolean   // ← NOVO
}

export interface UpdateCourseRequest {
  name: string
  coordinatorId: number
  years: number
  expectedCohortsPerYear: Record<number, number>
  hasBusinessSimulation: boolean   // ← NOVO
}

export interface CoordinatorOption {
  id: number
  name: string
}

