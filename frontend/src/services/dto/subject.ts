export interface CreateSubjectRequest {
  name: string,
  courseId: number,
  teacherIds: number[]
}

export interface UpdateSubjectRequest {
  name: string,
  teacherIds: number[]
}

export interface SubjectDetailResponse {
  id: number,
  name: string,
  courseId: number,
  courseName: string,
  teachers: TeacherInfo[]
}

export interface SubjectListResponse {
  id: number,
  name: string,
  teachers: TeacherInfo[]
}

export interface TeacherInfo {
  id: number,
  name: string
}
