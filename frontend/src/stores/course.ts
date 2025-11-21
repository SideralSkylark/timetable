import { defineStore } from 'pinia'
import { courseService } from '@/services/courseService'
import type { CourseResponse } from '@/services/dto/course'

export const useCourseStore = defineStore('course', {
  state: () => ({
    pagedCourses: null as any,
  }),

  actions: {
    async fetchCourses(page = 0, size = 10) {
      this.pagedCourses = await courseService.getAll(page, size)
    },

    async createCourse(data: any) {
      return await courseService.create(data)
    },

    async updateCourse(id: number, data: any) {
      return await courseService.update(id, data)
    },

    async deleteCourse(id: number) {
      await courseService.delete(id)
    },
  },
})
