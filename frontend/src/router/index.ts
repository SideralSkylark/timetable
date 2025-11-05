import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import Dashboard from '@/views/Dashboard.vue'
import Users from '@/views/Users.vue'
import Reports from '@/views/Reports.vue'
import Login from '@/views/Login.vue'
import NotFound from '@/views/NotFound.vue'
import Forbiden from '@/views/Forbidden.vue'
import Forbidden from '@/views/Forbidden.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Login',
      component: Login,
      meta: { requiresGuest: true },
    },
    {
      path: '/dashboard',
      component: DashboardLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'DashboardHome',
          component: Dashboard,
          meta: { roles: ['USER', 'ADMIN'], label: 'Início' },
        },
        {
          path: 'users',
          name: 'Users',
          component: Users,
          meta: { roles: ['ADMIN'], label: 'Utilizadores' },
        },
        {
          path: 'reports',
          name: 'Reports',
          component: Reports,
          meta: { roles: ['ADMIN', 'USER'], label: 'Relatórios' },
        },
      ],
    },
    { path: '/forbidden', name: 'Forbidden', component: Forbidden, meta: { label: "Acesso negado "} },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFound },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'Login' }
  }

  if (to.meta.requiresGuest && auth.isAuthenticated) {
    return { name: 'DashboardHome' }
  }

  // Validação de roles
  if (to.meta.roles) {
    const roles = auth.user?.roles ?? []
    const allowed = (to.meta.roles as string[]).some((r) => roles.includes(r))
    if (!allowed) {
      return { name: 'Forbidden' }
    }
  }

  return true
})

export default router
