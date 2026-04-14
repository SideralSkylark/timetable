import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import MyTimetableView from '../MyTimetableView.vue'
import { useAuthStore } from '@/stores/auth'
import { Role } from '@/services'
import { timetableService } from '@/services/timetableService'

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn()
}))

vi.mock('@/services/timetableService', () => ({
  timetableService: {
    loadMyTeacherTimetable: vi.fn(),
    loadMyStudentTimetable: vi.fn()
  }
}))

vi.mock('lucide-vue-next', () => ({
  CalendarCheck: { render: () => null },
  CalendarDays: { render: () => null },
  ChevronDown: { render: () => null },
  Loader2: { render: () => null },
}))

describe('MyTimetableView.vue', () => {
  let authStoreMock: any

  beforeEach(() => {
    setActivePinia(createPinia())
    
    authStoreMock = {
      user: { roles: [Role.STUDENT] }
    }
    vi.mocked(useAuthStore).mockReturnValue(authStoreMock)
    vi.clearAllMocks()
  })

  const factory = () => {
    return mount(MyTimetableView, {
      global: {
        stubs: {
          PageHeader: true,
          FilterBar: true,
          Transition: true
        }
      }
    })
  }

  it('identifies role correctly', () => {
    authStoreMock.user.roles = [Role.TEACHER]
    const wrapper = factory()
    const vm = wrapper.vm as any
    expect(vm.isTeacher).toBe(true)
    expect(vm.isStudent).toBe(false)
    
    authStoreMock.user.roles = [Role.STUDENT]
    const wrapper2 = factory()
    const vm2 = wrapper2.vm as any
    expect(vm2.isTeacher).toBe(false)
    expect(vm2.isStudent).toBe(true)
  })

  it('calls correct service method for TEACHER', async () => {
    authStoreMock.user.roles = [Role.TEACHER]
    vi.mocked(timetableService.loadMyTeacherTimetable).mockResolvedValue({ lessonAssignments: [] } as any)
    
    factory()
    
    expect(timetableService.loadMyTeacherTimetable).toHaveBeenCalled()
    expect(timetableService.loadMyStudentTimetable).not.toHaveBeenCalled()
  })

  it('calls correct service method for STUDENT', async () => {
    authStoreMock.user.roles = [Role.STUDENT]
    vi.mocked(timetableService.loadMyStudentTimetable).mockResolvedValue({ lessonAssignments: [] } as any)
    
    factory()
    
    expect(timetableService.loadMyStudentTimetable).toHaveBeenCalled()
    expect(timetableService.loadMyTeacherTimetable).not.toHaveBeenCalled()
  })

  it('extracts cell lessons correctly', async () => {
    const mockLesson = {
      id: 1,
      timeslot: { dayOfWeek: 'MONDAY', startTime: '07:00:00' },
      cohort: { id: 1, year: 1, displayName: 'C1' },
      subject: { name: 'Math' }
    }
    vi.mocked(timetableService.loadMyStudentTimetable).mockResolvedValue({ 
      lessonAssignments: [mockLesson] 
    } as any)

    const wrapper = factory()
    const vm = wrapper.vm as any
    
    // Wait for mock data
    await vi.waitFor(() => expect(vm.loading).toBe(false))
    
    const cellLessons = vm.getCellLessons('MONDAY', '07:00')
    expect(cellLessons).toHaveLength(1)
    expect(cellLessons[0].id).toBe(1)
  })

  it('returns correct year color classes', () => {
    const wrapper = factory()
    const vm = wrapper.vm as any
    expect(vm.yearColorClass(1)).toContain('bg-green-100')
    expect(vm.yearColorClass(3)).toContain('bg-orange-100')
    expect(vm.yearColorClass(99)).toContain('bg-gray-100')
  })
})
