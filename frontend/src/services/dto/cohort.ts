// cohort.ts
export type CohortStatus = 'ESTIMATED' | 'CONFIRMED'

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

export interface CohortResponse {
  id: number
  year: number
  section: string
  academicYear: number
  semester: number
  courseId: number
  courseName: string
  studentCount: number
  status: CohortStatus
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

// NOVO — para confirmar ingressos
export interface ConfirmCohortRequest {
  studentCount: number  // número real de alunos
}
