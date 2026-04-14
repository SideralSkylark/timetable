import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'
import { authService, Role } from '@/services'

vi.mock('@/services', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    authService: {
      login: vi.fn(),
      logout: vi.fn(),
      getCurrentUser: vi.fn(() => null),
      isAuthenticated: vi.fn(() => false),
    },
  }
})

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initializes with default values', () => {
    const store = useAuthStore()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(store.loading).toBe(false)
  })

  it('sets user on successful login', async () => {
    const mockUser = { id: 1, username: 'test', email: 'test@example.com', roles: [Role.ADMIN] }
    vi.mocked(authService.login).mockResolvedValue(mockUser)

    const store = useAuthStore()
    await store.login('test@example.com', 'password')

    expect(store.user).toEqual(mockUser)
    expect(store.isAuthenticated).toBe(true)
    expect(store.loading).toBe(false)
    expect(store.isAdmin).toBe(true)
  })

  it('sets error on failed login', async () => {
    const errorMessage = 'Invalid credentials'
    vi.mocked(authService.login).mockRejectedValue({
      response: { data: { message: errorMessage } }
    })

    const store = useAuthStore()
    try {
      await store.login('test@example.com', 'wrong')
    } catch (e) {
      // Expected
    }

    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(store.error).toBe(errorMessage)
    expect(store.loading).toBe(false)
  })

  it('clears user on logout', async () => {
    const store = useAuthStore()
    store.user = { id: 1, username: 'test', email: 'test@example.com', roles: [] }
    store.isAuthenticated = true

    vi.mocked(authService.logout).mockResolvedValue(undefined)

    await store.logout()

    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
})
