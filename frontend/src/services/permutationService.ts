import api from './api'

export interface ValidSlot {
  timeslotId: number
  dayOfWeek: string
  startTime: string
  endTime: string
  roomId: number
  roomName: string
}

export const permutationService = {
  getValidSlots: async (
    scheduledClassId: number,
    academicYear: number,
    semester: number,
  ): Promise<ValidSlot[]> => {
    const res = await api.post<ValidSlot[]>('/v1/permutations/valid-slots', {
      scheduledClassId,
      academicYear,
      semester,
    })
    return res.data
  },

  applySwap: async (
    scheduledClassId: number,
    targetTimeslotId: number,
    targetRoomId: number,
  ): Promise<void> => {
    await api.post('/v1/permutations/apply', {
      scheduledClassId,
      targetTimeslotId,
      targetRoomId,
    })
  },
}
