import api from './api'
import type { TimetableSolution, GenerationStartedResponse, JobStatusResponse } from './dto/timetable'

export const timetableService = {
  generate: async (academicYear: number, semester: number): Promise<GenerationStartedResponse> => {
    const res = await api.post<GenerationStartedResponse>('/v1/solver/generate', { academicYear, semester })
    return res.data
  },

  getSolution: async (jobId: string): Promise<TimetableSolution | null> => {
    const res = await api.get(`/v1/solver/${jobId}`)
    if (res.status === 202) return null
    return res.data as TimetableSolution
  },

  /**
   * Loads a persisted timetable from the DB by year + semester.
   * Returns null if not yet generated (404).
   */
  loadPersisted: async (academicYear: number, semester: number): Promise<TimetableSolution | null> => {
    try {
      const res = await api.get(`/v1/timetables/${academicYear}/${semester}`)
      return res.data as TimetableSolution
    } catch (e: any) {
      if (e?.response?.status === 404) return null
      throw e
    }
  },

  getStatus: async (jobId: string): Promise<JobStatusResponse> => {
    const res = await api.get<JobStatusResponse>(`/v1/solver/${jobId}/status`)
    return res.data
  },
}
