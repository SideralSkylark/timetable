export interface CreateCohortRequest {
  name: string
  description?: string
  startDate: string
  endDate?: string
}

export interface UpdateCohortRequest {
  name?: string
  description?: string
  startDate?: string
  endDate?: string
}

export interface CohortResponse {
  id: number
  name: string
  description?: string
  startDate: string
  endDate?: string
  createdAt: string
  updatedAt: string
}
