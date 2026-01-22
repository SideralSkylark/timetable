export interface CreateSubjectRequest {
  name: string,
  credits: number,
  targetYear: number,
  targetSemester: number,
  courseId: number,
  eligibleTeacherIds: number[]
}

export interface UpdateSubjectRequest {
  name: string,
  credits: number,
  targetYear: number,
  targetSemester: number,
  eligibleTeacherIds: number[]
}

export interface SubjectDetailResponse {
  id: number,
  name: string,
  credits: number,
  targetYear: number,
  targetSemester: number,
  courseId: number,
  courseName: string,
  eligibleTeacherIds: number[]
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
