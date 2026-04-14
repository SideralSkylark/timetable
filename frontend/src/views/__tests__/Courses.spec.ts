import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { reactive, nextTick } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import Courses from '../Courses.vue'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import { courseService } from '@/services/courseService'
import { subjectService } from '@/services/subjectService'
import { teacherService } from '@/services/teacherService'

// Mocking services and stores
vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn()
}))
vi.mock('@/composables/useToast', () => ({
  useToast: vi.fn()
}))
vi.mock('@/services/courseService', () => ({
  courseService: {
    getAll: vi.fn(),
    getCoordinators: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn()
  }
}))
vi.mock('@/services/subjectService', () => ({
  subjectService: {
    getAllByCourse: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn()
  }
}))
vi.mock('@/services/teacherService', () => ({
  teacherService: {
    getAll: vi.fn()
  }
}))

// Mock lucide-vue-next
vi.mock('lucide-vue-next', () => ({
  GraduationCap: { render: () => null },
  Plus: { render: () => null },
  Edit: { render: () => null },
  Edit2: { render: () => null },
  Trash2: { render: () => null },
  ChevronDown: { render: () => null },
  ChevronRight: { render: () => null },
  BookOpen: { render: () => null },
  User: { render: () => null },
  Tag: { render: () => null },
  Calendar: { render: () => null },
  Minus: { render: () => null },
  X: { render: () => null },
  Save: { render: () => null },
  Check: { render: () => null },
  Search: { render: () => null },
  Loader2: { render: () => null },
  Users: { render: () => null },
  PlusIcon: { render: () => null },
}))

describe('Courses.vue Business Logic', () => {
  let userStoreMock: any
  let toastMock: any

  beforeEach(() => {
    setActivePinia(createPinia())
    
    toastMock = { success: vi.fn(), error: vi.fn() }
    vi.mocked(useToast).mockReturnValue(toastMock)

    userStoreMock = reactive({
      myHighestRole: 'ADMIN',
      currentUser: { id: 1 },
      fetchCurrentUser: vi.fn()
    })
    vi.mocked(useUserStore).mockReturnValue(userStoreMock)

    vi.mocked(courseService.getAll).mockResolvedValue({ content: [], page: {} } as any)
    vi.mocked(courseService.getCoordinators).mockResolvedValue({ content: [], page: {} } as any)
  })

  const factory = () => {
    return mount(Courses, {
      global: {
        stubs: {
          PageHeader: true,
          FilterBar: true,
          Transition: true,
          'router-link': true
        }
      }
    })
  }

  describe('Role Helpers', () => {
    it('identifies admin correctly', () => {
      userStoreMock.myHighestRole = 'ADMIN'
      const wrapper = factory()
      expect((wrapper.vm as any).isAdmin).toBe(true)

      userStoreMock.myHighestRole = 'COORDINATOR'
      const wrapper2 = factory()
      expect((wrapper2.vm as any).isAdmin).toBe(false)
    })

    it('identifies owner correctly', () => {
      userStoreMock.currentUser = { id: 10 }
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      expect(vm.isOwner({ coordinatorId: 10 })).toBe(true)
      expect(vm.isOwner({ coordinatorId: 99 })).toBe(false)
    })
  })

  describe('Filtering', () => {
    it('calculates activeFilterCount correctly', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any

      expect(vm.activeFilterCount).toBe(0)
      vm.filters.name = 'Eng'
      expect(vm.activeFilterCount).toBe(1)
      vm.filters.hasBusinessSimulation = true
      expect(vm.activeFilterCount).toBe(2)
    })

    it('filters courses correctly', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      vm.courses = [
        { id: 1, name: 'Engenharia', coordinatorId: 1, years: 5, hasBusinessSimulation: true },
        { id: 2, name: 'Gestão', coordinatorId: 2, years: 3, hasBusinessSimulation: false }
      ]

      vm.filters.name = 'Eng'
      expect(vm.filteredCourses).toHaveLength(1)
      expect(vm.filteredCourses[0].id).toBe(1)

      vm.clearFilters()
      vm.filters.years = 3
      expect(vm.filteredCourses).toHaveLength(1)
      expect(vm.filteredCourses[0].id).toBe(2)
    })
  })

  describe('Course Expansion', () => {
    it('toggles expansion and loads subjects if empty', async () => {
      vi.mocked(subjectService.getAllByCourse).mockResolvedValue({ content: [{ id: 101, name: 'Math' }], page: {} } as any)
      
      const wrapper = factory()
      const vm = wrapper.vm as any
      const course = reactive({ id: 1, expanded: false, disciplines: [], loadingSubjects: false })
      
      await vm.toggleCourse(course)
      expect(course.expanded).toBe(true)
      expect(subjectService.getAllByCourse).toHaveBeenCalledWith(1, 0, 100)
      expect(course.disciplines).toHaveLength(1)
    })
  })

  describe('Course Form Validation', () => {
    it('validates required fields for course', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      vm.openCreateModal()
      vm.courseForm.name = ''
      vm.courseForm.coordinatorId = ''
      
      await vm.handleCourseSubmit()
      
      expect(vm.courseFormErrors.name).toBe(true)
      expect(vm.courseFormErrors.coordinatorId).toBe(true)
      expect(toastMock.error).toHaveBeenCalled()
    })
  })
})
