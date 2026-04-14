import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Login from '../Login.vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { Role } from '@/services'

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: vi.fn()
}))

vi.mock('lucide-vue-next', () => ({
  GraduationCap: { render: () => null },
  Mail: { render: () => null },
  Lock: { render: () => null },
  Eye: { render: () => null },
  EyeOff: { render: () => null },
  AlertCircle: { render: () => null },
  Loader2: { render: () => null },
  LogIn: { render: () => null },
}))

describe('Login.vue', () => {
  let authStoreMock: any
  let routerMock: any

  beforeEach(() => {
    setActivePinia(createPinia())
    
    authStoreMock = {
      login: vi.fn(),
      user: null
    }
    vi.mocked(useAuthStore).mockReturnValue(authStoreMock)

    routerMock = {
      push: vi.fn()
    }
    vi.mocked(useRouter).mockReturnValue(routerMock)
  })

  it('redirects to MyTimetableView for STUDENT role', async () => {
    authStoreMock.login.mockResolvedValueOnce(undefined)
    authStoreMock.user = { roles: [Role.STUDENT] }
    
    const wrapper = mount(Login)
    const vm = wrapper.vm as any
    
    vm.email = 'student@test.com'
    vm.password = 'password'
    
    await vm.handleLogin()
    
    expect(authStoreMock.login).toHaveBeenCalledWith('student@test.com', 'password')
    expect(routerMock.push).toHaveBeenCalledWith({ name: 'MyTimetableView' })
  })

  it('redirects to DashboardHome for ADMIN role', async () => {
    authStoreMock.login.mockResolvedValueOnce(undefined)
    authStoreMock.user = { roles: [Role.ADMIN] }
    
    const wrapper = mount(Login)
    const vm = wrapper.vm as any
    
    vm.email = 'admin@test.com'
    vm.password = 'password'
    
    await vm.handleLogin()
    
    expect(routerMock.push).toHaveBeenCalledWith({ name: 'DashboardHome' })
  })

  it('shows error message on failed login', async () => {
    authStoreMock.login.mockRejectedValueOnce(new Error('Invalid'))
    
    const wrapper = mount(Login)
    const vm = wrapper.vm as any
    
    await vm.handleLogin()
    
    expect(vm.error).toBe('Credenciais inválidas. Tente novamente.')
    expect(routerMock.push).not.toHaveBeenCalled()
  })

  it('toggles password visibility', async () => {
    const wrapper = mount(Login)
    const vm = wrapper.vm as any
    
    expect(vm.showPassword).toBe(false)
    
    const toggleBtn = wrapper.find('button[type="button"]')
    await toggleBtn.trigger('click')
    
    expect(vm.showPassword).toBe(true)
  })
})
