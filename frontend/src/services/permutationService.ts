import api from './api'

export interface ValidSlot {
  timeslotId: number
  dayOfWeek: string
  startTime: string
  endTime: string
  isSwap: boolean
  swapWithId: number | null
  swapWithSubject: string | null
  swapWithCohort: string | null
  roomName: string        // ← NOVO
  roomId: number          // ← NOVO
}

export const permutationService = {
  getValidSlots: async (
    scheduledClassId: number,
    academicYear: number,
    semester: number,
  ): Promise<ValidSlot[]> => {
    const res = await api.post<ValidSlot[]>('/v1/permutations/valid-slots', {
      scheduledClassId, academicYear, semester,
    })
    return res.data
  },

  applySwap: async (
    scheduledClassId: number,
    targetTimeslotId: number,
    targetRoomId: number,   // ← NOVO
    swapWithId: number | null,
  ): Promise<void> => {
    await api.post('/v1/permutations/apply', {
      scheduledClassId,
      targetTimeslotId,
      targetRoomId,          // ← NOVO
      swapWithId,
    })
  },
}
