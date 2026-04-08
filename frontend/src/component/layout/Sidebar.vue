<template>
  <aside class="w-60 bg-white border-r border-gray-100 flex flex-col">
    <!-- Brand -->
    <div class="px-5 py-5 border-b border-gray-100">
      <div class="flex items-center gap-3">
        <div class="bg-blue-900 p-2 rounded-lg">
          <GraduationCap class="w-5 h-5 text-white" />
        </div>
        <div>
          <h2 class="text-sm font-semibold text-gray-900">UCM-FEG</h2>
          <p class="text-xs text-gray-400">Sistema de gestão</p>
        </div>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 px-3 py-4 space-y-0.5">
      <RouterLink
        v-for="item in allowedRoutes"
        :key="item.name"
        :to="{ name: item.name }"
        class="group flex items-center gap-2.5 px-2.5 py-2 text-sm rounded-lg transition-colors"
        :class="isActive(item)
          ? 'bg-blue-900 text-white'
          : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'"
      >
        <component
          :is="item.icon"
          class="w-4 h-4 flex-shrink-0"
          :class="isActive(item) ? 'text-white' : 'text-gray-400 group-hover:text-blue-900'"
        />
        <span class="truncate font-medium">{{ item.label }}</span>
        <ChevronRight
          v-if="isActive(item)"
          class="w-3.5 h-3.5 ml-auto text-white/60"
        />
      </RouterLink>
    </nav>

    <!-- User footer -->
    <div class="px-3 py-3 border-t border-gray-100">
      <!-- User info -->
      <div class="flex items-center gap-2.5 px-2.5 py-2 rounded-lg mb-0.5">
        <div class="w-7 h-7 rounded-full bg-blue-900 flex items-center justify-center shrink-0">
          <span class="text-xs font-medium text-white leading-none">{{ userInitials }}</span>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-xs font-medium text-gray-800 truncate">
            {{ auth.user?.username ?? 'Utilizador' }}
          </p>
          <p class="text-xs text-gray-400 truncate">
            {{ userRoleLabel }}
          </p>
        </div>
      </div>
      <!-- Logout -->
      <button
        @click="logout"
        class="w-full flex items-center gap-2.5 px-2.5 py-2 text-sm rounded-lg text-gray-500 hover:bg-red-50 hover:text-red-600 transition-colors group"
      >
        <LogOut class="w-4 h-4 text-gray-400 group-hover:text-red-500 transition-colors" />
        <span class="font-medium">Terminar sessão</span>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import {
  Home,
  Building,
  School,
  Users as UsersIcon,
  GraduationCap as CohortIcon,
  LogOut,
  GraduationCap,
  ChevronRight,
  CalendarDays,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const userStore = useUserStore()

const roleLabels: Record<string, string> = {
  ADMIN:       'Administrador',
  COORDINATOR: 'Coordenador',
  DIRECTOR:    'Diretor',
  ASISTENT:    'Assistente',
  STUDENT:     'Estudante',
  TEACHER:     'Docente',
  USER:        'Utilizador',
}

const userInitials = computed(() => {
  const name = auth.user?.username ?? ''
  return name.slice(0, 2).toUpperCase()
})

const userRoleLabel = computed(() => {
  const role = userStore.myHighestRole
  return roleLabels[role] ?? role
})

const dashboardRoutes = [
  { name: 'DashboardHome',  label: 'Início',       icon: Home,        roles: ['USER'] },
  { name: 'Rooms',          label: 'Salas',         icon: Building,    roles: ['ADMIN', 'ASISTENT', 'DIRECTOR'] },
  { name: 'Courses',        label: 'Cursos',        icon: School,      roles: ['ADMIN', 'COORDINATOR'] },
  { name: 'Cohorts',        label: 'Turmas',        icon: CohortIcon,  roles: ['ADMIN', 'COORDINATOR', 'ASISTENT', 'DIRECTOR'] },
  { name: 'Users',          label: 'Utilizadores',  icon: UsersIcon,   roles: ['ADMIN', 'ASISTENT', 'DIRECTOR'] },
  { name: 'Timetable',      label: 'Horários',      icon: CalendarDays, roles: ['ADMIN', 'COORDINATOR', 'ASISTENT', 'DIRECTOR'] },
  { name: 'MyTimetableView', label: 'Meu Horário',  icon: CalendarDays, roles: ['STUDENT', 'TEACHER'] },
]

const allowedRoutes = computed(() => {
  const roles = auth.user?.roles ?? []
  return dashboardRoutes.filter(item =>
    item.roles.some(role => roles.includes(role))
  )
})

const isActive = (item: typeof dashboardRoutes[number]) => route.name === item.name

const logout = async () => {
  await auth.logout()
  router.push({ name: 'Login' })
}
</script>
