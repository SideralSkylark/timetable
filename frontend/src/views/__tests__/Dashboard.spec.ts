import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { reactive, nextTick } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import Dashboard from '../Dashboard.vue'
import { Role } from '@/services'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { useRoomStore } from '@/stores/room'
import { useCourseStore } from '@/stores/course'
import { useCohortStore } from '@/stores/cohorts'

// Mocking stores
vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn()
}))
vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn()
}))
vi.mock('@/stores/room', () => ({
  useRoomStore: vi.fn()
}))
vi.mock('@/stores/course', () => ({
  useCourseStore: vi.fn()
}))
vi.mock('@/stores/cohorts', () => ({
  useCohortStore: vi.fn()
}))

// Mock lucide-vue-next
vi.mock('lucide-vue-next', () => ({
  LayoutDashboard: { render: () => null },
  User: { render: () => null },
  Users: { render: () => null },
  DoorOpen: { render: () => null },
  BookOpen: { render: () => null },
  GraduationCap: { render: () => null },
  Calendar: { render: () => null },
  ArrowRight: { render: () => null },
  Info: { render: () => null },
  ShieldCheck: { render: () => null },
  Zap: { render: () => null },
  Clock: { render: () => null },
}))

describe('Dashboard.vue', () => {
  let authStoreMock: any
  let userStoreMock: any
  let roomStoreMock: any
  let courseStoreMock: any
  let cohortStoreMock: any

  beforeEach(() => {
    setActivePinia(createPinia())

    authStoreMock = reactive({
      username: 'testuser',
      roles: [Role.ADMIN]
    })
    vi.mocked(useAuthStore).mockReturnValue(authStoreMock)

    userStoreMock = reactive({
      fetchUsers: vi.fn(),
      pagedUsers: { page: { totalElements: 10 } }
    })
    vi.mocked(useUserStore).mockReturnValue(userStoreMock)

    roomStoreMock = reactive({
      fetchRooms: vi.fn(),
      pagedRooms: { page: { totalElements: 5 } }
    })
    vi.mocked(useRoomStore).mockReturnValue(roomStoreMock)

    courseStoreMock = reactive({
      fetchCourses: vi.fn(),
      pagedCourses: { page: { totalElements: 3 } }
    })
    vi.mocked(useCourseStore).mockReturnValue(courseStoreMock)

    cohortStoreMock = reactive({
      fetchCohorts: vi.fn(),
      cohortsPage: { page: { totalElements: 8 } }
    })
    vi.mocked(useCohortStore).mockReturnValue(cohortStoreMock)
  })

  const factory = () => {
    return mount(Dashboard, {
      global: {
        stubs: {
          PageHeader: true,
          'router-link': true
        }
      }
    })
  }

  describe('Role-based visibility', () => {
    it('identifies staff roles correctly', async () => {
      authStoreMock.roles = [Role.ADMIN]
      let wrapper = factory()
      expect((wrapper.vm as any).isStaff).toBe(true)

      authStoreMock.roles = [Role.STUDENT]
      wrapper = factory()
      expect((wrapper.vm as any).isStaff).toBe(false)
    })

    it('identifies hasTimetable correctly', async () => {
      authStoreMock.roles = [Role.STUDENT]
      let wrapper = factory()
      expect((wrapper.vm as any).hasTimetable).toBe(true)

      authStoreMock.roles = [Role.ADMIN]
      wrapper = factory()
      expect((wrapper.vm as any).hasTimetable).toBe(false)
    })
  })

  describe('Stats Calculation', () => {
    it('populates stats on mount if staff', async () => {
      authStoreMock.roles = [Role.ADMIN]
      const wrapper = factory()
      const vm = wrapper.vm as any

      // Wait for promises in onMounted
      await vi.waitFor(() => expect(vm.statValues.loading).toBe(false))

      expect(vm.statValues.users).toBe(10)
      expect(vm.statValues.rooms).toBe(5)
      expect(vm.statValues.courses).toBe(3)
      expect(vm.statValues.cohorts).toBe(8)
    })

    it('filters stats based on roles', async () => {
      authStoreMock.roles = [Role.COORDINATOR]
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      // COORDINATOR sees Users, Rooms, Courses (role specific), and Cohorts
      expect(vm.stats).toHaveLength(4)
      
      authStoreMock.roles = [Role.ASISTENT]
      const wrapper2 = factory()
      const vm2 = wrapper2.vm as any
      
      // ASISTENT doesn't have Role.COORDINATOR, so they only see 3 stats (Users, Rooms, Turmas)
      // Cursos has roles: ['ADMIN', 'COORDINATOR']
      expect(vm2.stats).toHaveLength(3)
      expect(vm2.stats.find((s: any) => s.label === 'Cursos')).toBeUndefined()
    })
  })

  describe('Quick Actions', () => {
    it('returns correct actions for ADMIN', () => {
      authStoreMock.roles = [Role.ADMIN]
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      // Admin sees: Gerar Horário, Gestão de Salas, Disciplinas & Cursos, Visualizar Horários
      expect(vm.quickActions).toHaveLength(4)
    })

    it('returns limited actions for COORDINATOR', () => {
      authStoreMock.roles = [Role.COORDINATOR]
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      // Coordinator sees: Disciplinas & Cursos, Visualizar Horários
      expect(vm.quickActions).toHaveLength(2)
      expect(vm.quickActions[0].title).toBe('Disciplinas & Cursos')
    })

    it('returns minimal actions for STUDENT', () => {
      authStoreMock.roles = [Role.STUDENT]
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      expect(vm.quickActions).toHaveLength(1)
      expect(vm.quickActions[0].title).toBe('Visualizar Horários')
    })
  })
})
