import { defineStore } from 'pinia'
import { subjectService } from '@/services/subjectService'

export const useSubjectStore = defineStore('subject', {
  actions: {
    async createSubject(data: any) {
      return await subjectService.create(data)
    },

    async deleteSubject(id: number) {
      return await subjectService.delete(id)
    },
  },
})

