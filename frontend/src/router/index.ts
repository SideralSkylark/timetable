import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import Dashboard from '@/views/Dashboard.vue'
import Users from '@/views/Users.vue'
import Login from '@/views/Login.vue'
import NotFound from '@/views/NotFound.vue'
import Cohorts from '@/views/Cohorts.vue'
import Timetable from '@/views/Timetable.vue'
import Forbidden from '@/views/Forbidden.vue'
import Rooms from '@/views/Rooms.vue'
import Courses from '@/views/Courses.vue'

const routes = [
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
        meta: { roles: ['USER'], label: 'Início' },
      },
      {
        path: 'rooms',
        name: 'Rooms',
        component: Rooms,
        meta: { roles: ['ADMIN', 'ASISTENT', 'DIRECTOR'], label: 'Salas' },
      },
      {
        path: 'courses',
        name: 'Courses',
        component: Courses,
        meta: { roles: ['ADMIN', 'COORDINATOR'], label: 'Cursos' },
      },
      { path: 'cohorts', name: 'Cohorts', component: Cohorts, meta: { roles: ['ADMIN', 'COORDINATOR', 'ASISTENT', 'DIRECTOR'], label: 'Turmas' } },
      {
        path: 'users',
        name: 'Users',
        component: Users,
        meta: { roles: ['ADMIN', 'ASISTENT', 'DIRECTOR'], label: 'Utilizadores' },
      },
      {
        path: 'timetable',
        name: 'Timetable',
        component: Timetable,
        meta: { roles: ['ADMIN', 'COORDINATOR', 'ASISTENT', 'DIRECTOR'], label: 'Horarios'},
      },
    ],
  },
  { path: '/forbidden', name: 'Forbidden', component: Forbidden },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFound },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'Login' }
  }

  if (to.meta.requiresGuest && auth.isAuthenticated) {
    return { name: 'DashboardHome' }
  }

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
