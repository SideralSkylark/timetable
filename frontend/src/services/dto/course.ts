export interface CreateCourseRequest {
  name: string,
  coordinatorId: number
}

export interface UpdateCourseRequest {
  name: string,
  coordinatorId: number
}

export interface CourseResponse {
  id: number,
  name: string,
  coordinatorId: number,
  createdAt: string
}
