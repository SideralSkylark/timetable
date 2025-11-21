<template>
  <aside class="w-64 bg-white border-r border-gray-200 flex flex-col">
    <!-- Header -->
    <div class="px-6 py-6 border-b border-gray-200">
      <div class="flex items-center gap-3">
        <div class="bg-blue-900 p-3 rounded-lg">
          <GraduationCap class="w-6 h-6 text-white" />
        </div>
        <div>
          <h2 class="text-lg font-bold text-gray-900">EduPanel</h2>
          <p class="text-xs text-gray-500">Sistema de Gestão</p>
        </div>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 px-4 py-6">
      <p class="px-3 mb-3 text-xs font-semibold text-gray-400 uppercase tracking-wider">
        Menu
      </p>
      <div class="space-y-1">
        <RouterLink
          v-for="item in allowedRoutes"
          :key="item.name"
          :to="{ name: item.name }"
          class="group flex items-center gap-3 px-3 py-2.5 text-sm font-medium rounded-lg transition"
          :class="[
            isActive(item)
              ? 'bg-blue-900 text-white'
              : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
          ]"
        >
          <component
            :is="item.icon"
            class="w-5 h-5 flex-shrink-0 transition"
            :class="isActive(item) ? 'text-white' : 'text-gray-400 group-hover:text-blue-900'"
          />
          <span class="truncate">{{ item.label }}</span>
          <ChevronRight
            v-if="isActive(item)"
            class="w-4 h-4 ml-auto text-white/70"
          />
        </RouterLink>
      </div>
    </nav>

    <!-- User & Logout -->
    <div class="px-4 py-4 border-t border-gray-200 space-y-3">
      <div class="flex items-center gap-3 px-3 py-2">
        <div class="w-9 h-9 rounded-full bg-blue-900 flex items-center justify-center">
          <User class="w-4 h-4 text-white" />
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-900 truncate">
            {{ auth.user?.username ?? 'Utilizador' }}
          </p>
          <p class="text-xs text-gray-500 truncate">
            {{ auth.user?.roles?.[0] ?? 'USER' }}
          </p>
        </div>
      </div>

      <button
        @click="logout"
        class="w-full flex items-center gap-3 px-3 py-2.5 text-sm font-medium rounded-lg
               text-gray-600 hover:bg-red-50 hover:text-red-600 transition group"
      >
        <LogOut class="w-5 h-5 text-gray-400 group-hover:text-red-600 transition" />
        <span>Terminar sessão</span>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Home,
  Building,
  School,
  Users,
  LogOut,
  GraduationCap,
  User,
  ChevronRight
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const dashboardRoutes = [
  { name: 'DashboardHome', label: 'Início', icon: Home, roles: ['USER', 'ADMIN'] },
  { name: 'Rooms', label: 'Salas', icon: Building, roles: ['ADMIN'] },
  { name: 'Courses', label: 'Cursos', icon: School, roles: ['ADMIN'] },
  { name: 'Users', label: 'Utilizadores', icon: Users, roles: ['ADMIN'] },
]

const allowedRoutes = computed(() => {
  const roles = auth.user?.roles ?? []
  return dashboardRoutes.filter((item) =>
    item.roles.some((role) => roles.includes(role))
  )
})

const isActive = (item: typeof dashboardRoutes[number]) => {
  return route.name === item.name
}

const logout = async () => {
  await auth.logout()
  router.push({ name: 'Login' })
}
</script>
