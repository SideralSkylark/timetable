import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { reactive, nextTick } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import Rooms from '../Rooms.vue'
import { useRoomStore } from '@/stores/room'
import { useCourseStore } from '@/stores/course'
import { useToast } from '@/composables/useToast'

// Mocking the stores and composables
vi.mock('@/stores/room', () => ({
  useRoomStore: vi.fn()
}))
vi.mock('@/stores/course', () => ({
  useCourseStore: vi.fn()
}))
vi.mock('@/composables/useToast', () => ({
  useToast: vi.fn()
}))

// Mock lucide-vue-next
vi.mock('lucide-vue-next', () => ({
  Building: { render: () => null },
  Plus: { render: () => null },
  DoorOpen: { render: () => null },
  Edit: { render: () => null },
  Tag: { render: () => null },
  Users: { render: () => null },
  X: { render: () => null },
  Check: { render: () => null },
  ShieldAlert: { render: () => null },
  Search: { render: () => null },
  ChevronDown: { render: () => null },
  Loader2: { render: () => null },
}))

describe('Rooms.vue Business Logic', () => {
  let roomStoreMock: any
  let courseStoreMock: any
  let toastMock: any

  beforeEach(() => {
    setActivePinia(createPinia())
    
    toastMock = { success: vi.fn(), error: vi.fn() }
    vi.mocked(useToast).mockReturnValue(toastMock)

    roomStoreMock = reactive({
      pagedRooms: {
        content: [],
        page: { totalPages: 1, totalElements: 0, size: 10, number: 0 }
      },
      fetchRooms: vi.fn(),
      createRoom: vi.fn(),
      updateRoom: vi.fn(),
      deleteRoom: vi.fn()
    })
    vi.mocked(useRoomStore).mockReturnValue(roomStoreMock)

    courseStoreMock = reactive({
      courses: [
        { id: 1, name: 'Engenharia Informática' },
        { id: 2, name: 'Gestão' }
      ],
      fetchAllCoursesSimple: vi.fn()
    })
    vi.mocked(useCourseStore).mockReturnValue(courseStoreMock)
  })

  const factory = () => {
    return mount(Rooms, {
      global: {
        stubs: {
          PageHeader: true,
          FilterBar: true,
          CrudTable: true,
          DeleteConfirmBanner: true,
          Transition: true
        }
      }
    })
  }

  describe('Filtering Logic', () => {
    it('calculates activeFilterCount correctly', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any

      expect(vm.activeFilterCount).toBe(0)

      vm.filters.name = 'Lab'
      expect(vm.activeFilterCount).toBe(1)

      vm.filters.capacityMin = 10
      expect(vm.activeFilterCount).toBe(2)

      vm.clearFilters()
      expect(vm.activeFilterCount).toBe(0)
    })

    it('triggers fetchRooms when filters change', async () => {
      factory()
      roomStoreMock.fetchRooms.mockClear()
      
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      vm.filters.name = 'Test'
      await nextTick()
      
      expect(roomStoreMock.fetchRooms).toHaveBeenCalled()
    })
  })

  describe('Data Mapping', () => {
    it('maps rooms with unique assigned course names', () => {
      roomStoreMock.pagedRooms.content = [
        {
          id: 1,
          name: 'A101',
          capacity: 50,
          restrictions: [
            { courseId: 1, courseName: 'LEI', period: 'MORNING' },
            { courseId: 1, courseName: 'LEI', period: 'AFTERNOON' },
            { courseId: 2, courseName: 'LGE', period: 'MORNING' }
          ]
        }
      ]
      const wrapper = factory()
      const vm = wrapper.vm as any

      expect(vm.filteredRooms[0].assignedCourses).toBe('LEI, LGE')
    })
  })

  describe('Form & Validation', () => {
    it('validates required fields', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any

      vm.openRoomModal()
      await nextTick()

      await vm.handleSubmit()

      expect(vm.formErrors.name).toBe(true)
      expect(vm.formErrors.capacity).toBe(true)
      expect(toastMock.error).toHaveBeenCalled()
    })

    it('builds period restrictions correctly', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any

      vm.formData.restrictions = [
        { courseId: 1, period: 'MORNING' },
        { courseId: 1, period: 'AFTERNOON' },
        { courseId: 2, period: 'MORNING' }
      ]

      const result = vm.buildPeriodRestrictions()
      expect(result).toEqual({
        MORNING: [1, 2],
        AFTERNOON: [1]
      })
    })
  })

  describe('UI Helpers', () => {
    it('returns correct capacity badge classes', () => {
      const wrapper = factory()
      const vm = wrapper.vm as any
      expect(vm.capacityBadgeClass(20)).toContain('bg-blue-50')
      expect(vm.capacityBadgeClass(50)).toContain('bg-blue-100')
      expect(vm.capacityBadgeClass(100)).toContain('bg-blue-200')
    })
  })
})
