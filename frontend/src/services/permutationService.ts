import api from './api'

export interface ValidSlot {
  timeslotId: number
  dayOfWeek: string
  startTime: string
  endTime: string
  isSwap: boolean
  swapWithId: number | null       // ScheduledClass.id of the displaced lesson
  swapWithSubject: string | null  // subject name for display
  swapWithCohort: string | null   // cohort display name for display
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
    swapWithId: number | null,
  ): Promise<void> => {
    await api.post('/v1/permutations/apply', {
      scheduledClassId,
      targetTimeslotId,
      swapWithId,   // null = move to empty slot
    })
  },
}
