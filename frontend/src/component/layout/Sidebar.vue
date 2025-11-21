<template>
  <aside class="w-64 bg-white border-r border-gray-200 flex flex-col shadow-sm">
    <!-- Header -->
    <div class="px-6 py-5 border-b border-gray-200 flex items-center gap-3">
      <div class="bg-blue-900 p-2 rounded-lg">
        <Home class="w-5 h-5 text-white" />
      </div>
      <div>
        <h2 class="text-xl font-bold text-gray-900">Painel</h2>
        <p class="text-xs text-gray-500">Navegação do sistema</p>
      </div>
    </div>

    <!-- Menu -->
    <nav class="flex-1 px-4 py-6 space-y-1">
      <RouterLink
        v-for="item in allowedRoutes"
        :key="item.name"
        :to="{ name: item.name }"
        class="group flex items-center gap-3 px-3 py-3 text-sm font-medium rounded-md transition-all duration-150"
        :class="[
          isActive(item)
            ? 'bg-blue-50 text-blue-900 border border-blue-100 shadow-sm'
            : 'text-gray-700 hover:bg-gray-50'
        ]"
      >
        <component
          :is="item.icon"
          class="w-5 h-5 flex-shrink-0 transition-colors"
          :class="isActive(item) ? 'text-blue-900' : 'text-gray-500 group-hover:text-blue-900'"
        />
        <span class="truncate">{{ item.label }}</span>
      </RouterLink>
    </nav>

    <!-- Logout -->
    <button
      @click="logout"
      class="mx-4 mb-6 mt-auto flex items-center gap-3 px-3 py-3 text-sm font-medium rounded-md border border-red-200 text-red-600 hover:bg-red-50 hover:border-red-300 transition"
    >
      <LogOut class="w-5 h-5 flex-shrink-0 text-red-600" />
      <span>Terminar sessão</span>
    </button>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Ícones
import { Home, Building, School, Users, LogOut } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// Rotas do dashboard
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
