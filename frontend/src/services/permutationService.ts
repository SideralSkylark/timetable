import api from './api'

export interface ValidSlot {
  timeslotId: number
  dayOfWeek: string
  startTime: string
  endTime: string
}

export const permutationService = {
  /**
   * Returns timeslots the given lesson can be moved to without violating constraints.
   */
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

  /**
   * Persists a confirmed swap.
   */
  applySwap: async (scheduledClassId: number, targetTimeslotId: number): Promise<void> => {
    await api.post('/v1/permutations/apply', { scheduledClassId, targetTimeslotId })
  },
}
