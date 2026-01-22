export interface CreateRoomRequest {
  name: string
  capacity: number
  restrictedToCourseId: number
}

export interface UpdateRoomRequest {
  name: string
  capacity: number
  restrictedToCourseId: number
}

export interface RoomResponse {
  id: number
  name: string
  capacity: number
  restrictedToCourseId: number
}
