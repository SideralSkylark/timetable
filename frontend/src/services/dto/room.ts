export interface CreateRoomRequest {
  name: string
  capacity: number
}

export interface UpdateRoomRequest {
  name: string
  capacity: number
}

export interface RoomResponse {
  id: number
  name: string
  capacity: number
}
