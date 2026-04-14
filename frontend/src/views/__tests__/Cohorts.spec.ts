import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { reactive, nextTick } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import Cohorts from '../Cohorts.vue'
import { useCohortStore } from '@/stores/cohorts'
import { useUserStore } from '@/stores/user'
import { useCourseStore } from '@/stores/course'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { Role } from '@/services'

// Mocking stores
vi.mock('@/stores/cohorts', () => ({
  useCohortStore: vi.fn()
}))
vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn()
}))
vi.mock('@/stores/course', () => ({
  useCourseStore: vi.fn()
}))
vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn()
}))
vi.mock('@/composables/useToast', () => ({
  useToast: vi.fn()
}))

// Mock lucide-vue-next
vi.mock('lucide-vue-next', () => ({
  Users: { render: () => null },
  Plus: { render: () => null },
  Edit: { render: () => null },
  UserPlus: { render: () => null },
  CheckCircle: { render: () => null },
  Clock: { render: () => null },
  AlertCircle: { render: () => null },
  Trash2: { render: () => null },
  Loader2: { render: () => null },
  X: { render: () => null },
  Check: { render: () => null },
  Search: { render: () => null },
  ChevronDown: { render: () => null },
  BookOpen: { render: () => null },
}))

describe('Cohorts.vue Business Logic', () => {
  let cohortStoreMock: any
  let courseStoreMock: any
  let authStoreMock: any
  let toastMock: any

  beforeEach(() => {
    setActivePinia(createPinia())
    
    toastMock = { success: vi.fn(), error: vi.fn() }
    vi.mocked(useToast).mockReturnValue(toastMock)

    authStoreMock = reactive({
      user: { id: 1, roles: [Role.ADMIN] }
    })
    vi.mocked(useAuthStore).mockReturnValue(authStoreMock)

    cohortStoreMock = reactive({
      cohortsPage: {
        content: [],
        page: { totalPages: 1 }
      },
      fetchCohorts: vi.fn(),
      confirmCohort: vi.fn(),
      createCohort: vi.fn(),
      updateCohort: vi.fn(),
      deleteCohort: vi.fn(),
      fetchMaxRoomCapacity: vi.fn(),
      maxRoomCapacity: 100
    })
    vi.mocked(useCohortStore).mockReturnValue(cohortStoreMock)

    courseStoreMock = reactive({
      courses: [
        { id: 1, name: 'C1', coordinatorId: 10 }
      ],
      fetchAllCoursesSimple: vi.fn()
    })
    vi.mocked(useCourseStore).mockReturnValue(courseStoreMock)
  })

  const factory = () => {
    return mount(Cohorts, {
      global: {
        stubs: {
          PageHeader: true,
          FilterBar: true,
          Pagination: true,
          DeleteConfirmBanner: true,
          Transition: true
        }
      }
    })
  }

  describe('Permissions', () => {
    it('allows ADMIN to manage any cohort', () => {
      authStoreMock.user.roles = [Role.ADMIN]
      const wrapper = factory()
      const vm = wrapper.vm as any
      expect(vm.canManageCohort({ courseId: 1 })).toBe(true)
      expect(vm.canManageCohort({ courseId: 99 })).toBe(true)
    })

    it('restricts COORDINATOR to their own course', () => {
      authStoreMock.user = { id: 10, roles: [Role.COORDINATOR] }
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      expect(vm.canManageCohort({ courseId: 1 })).toBe(true)
      expect(vm.canManageCohort({ courseId: 99 })).toBe(false)
    })
  })

  describe('Filtering', () => {
    it('calculates activeFilterCount correctly', () => {
      const wrapper = factory()
      const vm = wrapper.vm as any
      expect(vm.activeFilterCount).toBe(0)
      
      vm.filters.name = 'T1'
      expect(vm.activeFilterCount).toBe(1)
      
      vm.filters.semester = 1
      expect(vm.activeFilterCount).toBe(2)
    })

    it('maps cohorts with display name', () => {
      cohortStoreMock.cohortsPage.content = [
        { id: 1, year: 1, section: 'A', courseName: 'C1' }
      ]
      const wrapper = factory()
      const vm = wrapper.vm as any
      expect(vm.filteredCohorts[0].turma).toBe('1º Ano · Turma A')
    })
  })

  describe('Confirmation Logic', () => {
    it('validates student count on confirm', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      vm.openConfirmModal({ id: 1, studentCount: 35, turma: 'T1' })
      vm.confirmForm.studentCount = 0
      
      await vm.handleConfirm()
      expect(toastMock.error).toHaveBeenCalled()
      expect(cohortStoreMock.confirmCohort).not.toHaveBeenCalled()
    })
  })

  describe('Form Validation', () => {
    it('validates required fields for new cohort', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      vm.openCreateModal()
      vm.form.section = ''
      vm.form.courseId = ''
      
      await vm.handleSubmit()
      expect(vm.formErrors.section).toBe(true)
      expect(vm.formErrors.courseId).toBe(true)
    })
  })
})
