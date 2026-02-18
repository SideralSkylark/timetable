export interface CreateRoomRequest {
  name: string
  capacity: number
  restrictedToCourseId?: number
  periodRestrictions?: Record<string, number[]>
}

export interface UpdateRoomRequest {
  name: string
  capacity: number
  restrictedToCourseId?: number
  periodRestrictions?: Record<string, number[]>
}

export interface RoomRestriction {
  id: number
  courseId: number
  courseName: string
  period: string
}

export interface RoomResponse {
  id: number
  name: string
  capacity: number
  restrictions: RoomRestriction[]
}
