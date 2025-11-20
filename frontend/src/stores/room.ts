import { defineStore } from 'pinia'
import { roomService } from '@/services/roomService'
import type { RoomResponse, CreateRoomRequest, UpdateRoomRequest } from '@/services/dto/room'
import type { Page } from '@/services/types/page'

interface State {
  rooms: RoomResponse[]
  pagedRooms: Page<RoomResponse> | null
  loading: boolean
  error: string | null
}

export const useRoomStore = defineStore('room', {
  state: (): State => ({
    rooms: [],
    pagedRooms: null,
    loading: false,
    error: null,
  }),

  actions: {
    async fetchRooms(page = 0, size = 10) {
      this.loading = true
      try {
        const paged = await roomService.getAll(page, size)
        this.pagedRooms = paged
        this.rooms = paged.content
        this.error = null
      } catch (err: any) {
        this.error = err.message || 'Falha ao buscar salas'
      } finally {
        this.loading = false
      }
    },

    async createRoom(data: CreateRoomRequest) {
      this.loading = true
      try {
        const newRoom = await roomService.create(data)
        this.rooms.push(newRoom)
        this.error = null
        return newRoom
      } catch (err: any) {
        this.error = err.message || 'Falha ao criar sala'
        throw err
      } finally {
        this.loading = false
      }
    },

    async updateRoom(id: number, data: UpdateRoomRequest) {
      this.loading = true
      try {
        const updated = await roomService.update(id, data)
        const index = this.rooms.findIndex(r => r.id === id)
        if (index !== -1) this.rooms[index] = updated
        this.error = null
        return updated
      } catch (err: any) {
        this.error = err.message || 'Falha ao atualizar sala'
        throw err
      } finally {
        this.loading = false
      }
    },

    async deleteRoom(id: number) {
      this.loading = true
      try {
        await roomService.delete(id)
        this.rooms = this.rooms.filter(r => r.id !== id)
        this.error = null
      } catch (err: any) {
        this.error = err.message || 'Falha ao deletar sala'
        throw err
      } finally {
        this.loading = false
      }
    },
  },
})

