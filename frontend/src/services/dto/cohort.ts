export type CohortStatus = 'ESTIMATED' | 'CONFIRMED' | 'ACTIVE' | 'ARCHIVED'

// Used by GET /v1/cohorts (list)
export interface CohortListResponse {
  id: number
  year: number
  section: string
  academicYear: number
  semester: number
  courseId: number
  courseName: string
  studentCount: number
  status: CohortStatus
}

// Used by GET /v1/cohorts/:id, POST, PUT
export interface CohortResponse {
  id: number
  year: number
  section: string
  academicYear: number
  semester: number
  courseId: number
  courseName: string
  studentIds: number[]
}

export interface CreateCohortRequest {
  year: number
  section: string
  academicYear: number
  semester: number
  courseId: number
  studentIds?: number[]
}

export interface UpdateCohortRequest {
  year: number
  section: string
  academicYear: number
  semester: number
  studentIds: number[]
}
