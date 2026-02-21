import api from './api'
import type { TimetableSolution, GenerationStartedResponse, JobStatusResponse } from './dto/timetable'

export const timetableService = {
  /**
   * Starts async preparation + generation.
   * Returns jobId immediately — poll getSolution() until ready.
   */
  generate: async (academicYear: number, semester: number): Promise<GenerationStartedResponse> => {
    const res = await api.post<GenerationStartedResponse>('/v1/solver/generate', {
      academicYear,
      semester,
    })
    return res.data
  },

  /**
   * Poll this with the jobId returned by generate().
   * Returns null if still solving (202), or the solution when done (200).
   */
  getSolution: async (jobId: string): Promise<TimetableSolution | null> => {
    const res = await api.get(`/v1/solver/${jobId}`)
    if (res.status === 202) return null
    return res.data as TimetableSolution
  },

  /**
   * Get raw solver status for a job.
   */
  getStatus: async (jobId: string): Promise<JobStatusResponse> => {
    const res = await api.get<JobStatusResponse>(`/v1/solver/${jobId}/status`)
    return res.data
  },
}
