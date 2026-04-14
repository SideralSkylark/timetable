import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Users from '../Users.vue'
import { Role } from '@/services'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

// Mocking the stores and composables
vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn()
}))

vi.mock('@/composables/useToast', () => ({
  useToast: vi.fn()
}))

// Mock lucide-vue-next to avoid rendering issues
vi.mock('lucide-vue-next', () => ({
  Users: { render: () => null },
  Plus: { render: () => null },
  UserPlus: { render: () => null },
  Edit: { render: () => null },
  User: { render: () => null },
  Mail: { render: () => null },
  Lock: { render: () => null },
  Shield: { render: () => null },
  BookOpen: { render: () => null },
  X: { render: () => null },
  Check: { render: () => null },
  Search: { render: () => null },
  ChevronDown: { render: () => null },
  KeyRound: { render: () => null },
}))

describe('Users.vue Business Logic', () => {
  let userStoreMock: any
  let toastMock: any

  beforeEach(() => {
    setActivePinia(createPinia())
    
    toastMock = {
      success: vi.fn(),
      error: vi.fn()
    }
    vi.mocked(useToast).mockReturnValue(toastMock)

    userStoreMock = {
      myHighestRole: Role.ADMIN,
      currentUser: { id: 1, roles: [Role.ADMIN] },
      pagedUsers: {
        content: [],
        page: { totalPages: 1, totalElements: 0, size: 10, number: 0 }
      },
      fetchUsers: vi.fn(),
      createUser: vi.fn(),
      updateUser: vi.fn(),
      deleteUser: vi.fn(),
      resetPassword: vi.fn()
    }
    vi.mocked(useUserStore).mockReturnValue(userStoreMock)
  })

  const factory = () => {
    return mount(Users, {
      global: {
        stubs: {
          PageHeader: true,
          FilterBar: true,
          CrudTable: true,
          DeleteConfirmBanner: true,
          ResetPasswordModal: true,
          Transition: true
        }
      }
    })
  }

  describe('Permissions (canEdit / canDelete)', () => {
    it('allows ADMIN to edit and delete anyone', () => {
      userStoreMock.myHighestRole = Role.ADMIN
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      const otherAdmin = { id: 2, roles: [Role.ADMIN] }
      expect(vm.canEdit(otherAdmin)).toBe(true)
      expect(vm.canDelete(otherAdmin)).toBe(true)
    })

    it('limits DIRECTOR permissions correctly', () => {
      userStoreMock.myHighestRole = Role.DIRECTOR
      userStoreMock.currentUser = { id: 1, roles: [Role.DIRECTOR] }
      const wrapper = factory()
      const vm = wrapper.vm as any

      const adminUser = { id: 2, roles: [Role.ADMIN] }
      const otherDirector = { id: 3, roles: [Role.DIRECTOR] }
      const teacherUser = { id: 4, roles: [Role.TEACHER] }
      const self = { id: 1, roles: [Role.DIRECTOR] }

      // Can edit teachers and self, but not admins or other directors
      expect(vm.canEdit(adminUser)).toBe(false)
      expect(vm.canEdit(otherDirector)).toBe(false)
      expect(vm.canEdit(teacherUser)).toBe(true)
      expect(vm.canEdit(self)).toBe(true)

      // Can delete teachers, but not admins, directors (including self)
      expect(vm.canDelete(adminUser)).toBe(false)
      expect(vm.canDelete(otherDirector)).toBe(false)
      expect(vm.canDelete(teacherUser)).toBe(true)
      expect(vm.canDelete(self)).toBe(false)
    })

    it('limits ASISTENT permissions correctly', () => {
      userStoreMock.myHighestRole = Role.ASISTENT
      userStoreMock.currentUser = { id: 1, roles: [Role.ASISTENT] }
      const wrapper = factory()
      const vm = wrapper.vm as any

      const directorUser = { id: 2, roles: [Role.DIRECTOR] }
      const otherAssistant = { id: 3, roles: [Role.ASISTENT] }
      const teacherUser = { id: 4, roles: [Role.TEACHER] }
      const self = { id: 1, roles: [Role.ASISTENT] }

      // Can edit teachers and self, but not directors or other assistants
      expect(vm.canEdit(directorUser)).toBe(false)
      expect(vm.canEdit(otherAssistant)).toBe(false)
      expect(vm.canEdit(teacherUser)).toBe(true)
      expect(vm.canEdit(self)).toBe(true)

      // Can delete teachers, but not directors, assistants (including self)
      expect(vm.canDelete(directorUser)).toBe(false)
      expect(vm.canDelete(otherAssistant)).toBe(false)
      expect(vm.canDelete(teacherUser)).toBe(true)
      expect(vm.canDelete(self)).toBe(false)
    })
  })

  describe('Filtering Logic', () => {
    it('calculates activeFilterCount correctly', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any

      expect(vm.activeFilterCount).toBe(0)

      vm.filters.username = 'test'
      expect(vm.activeFilterCount).toBe(1)

      vm.filters.role = Role.TEACHER
      expect(vm.activeFilterCount).toBe(2)

      vm.clearFilters()
      expect(vm.activeFilterCount).toBe(0)
    })

    it('clears teacherType when switching to non-TEACHER role', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any

      vm.filters.role = Role.TEACHER
      vm.filters.teacherType = 'FULL_TIME'
      
      // We need to wait for the watcher
      vm.filters.role = Role.STUDENT
      await wrapper.vm.$nextTick()
      
      expect(vm.filters.teacherType).toBe('')
    })
  })

  describe('Form Validation', () => {
    it('validates required fields on submit', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      // Open modal to ensure form is reset
      vm.openUserModal()
      
      await vm.handleSubmit()
      
      expect(vm.formErrors.username).toBe(true)
      expect(vm.formErrors.email).toBe(true)
      expect(vm.formErrors.password).toBe(true)
      expect(toastMock.error).toHaveBeenCalledWith(expect.stringContaining('preencha todos os campos'))
    })

    it('validates email format', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      vm.openUserModal()
      vm.formData.username = 'testuser'
      vm.formData.email = 'invalid-email'
      vm.formData.password = 'password123'
      
      await vm.handleSubmit()
      
      expect(vm.formErrors.email).toBe(true)
    })

    it('requires teacherType for TEACHER role', async () => {
      const wrapper = factory()
      const vm = wrapper.vm as any
      
      vm.openUserModal()
      vm.formData.username = 'teacher'
      vm.formData.email = 'teacher@example.com'
      vm.formData.password = 'password123'
      vm.formData.selectedRoles = [Role.TEACHER]
      vm.formData.teacherType = null
      
      await vm.handleSubmit()
      
      expect(vm.formErrors.teacherType).toBe(true)
    })
  })
})
