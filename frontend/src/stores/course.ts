import { defineStore } from 'pinia'
import { courseService } from '@/services/courseService'
import type { CourseResponse } from '@/services/dto/course'

export const useCourseStore = defineStore('course', {
  state: () => ({
    pagedCourses: null as any,
    courses: [] as CourseResponse[], 
    loading: false,
  }),

  actions: {
    async fetchCourses(page = 0, size = 10) {
      this.loading = true
      try {
        this.pagedCourses = await courseService.getAll(page, size)
      } finally {
        this.loading = false
      }
    },

    async fetchAllCoursesSimple() {
      this.loading = true
      try {
        const paged = await courseService.getAll(0, 1000)
        this.courses = paged.content
      } finally {
        this.loading = false
      }
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
