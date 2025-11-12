<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Sidebar -->
    <aside class="w-64 bg-white border-r border-gray-200 flex flex-col">
      <div class="px-6 py-4 font-semibold text-gray-900 border-b border-gray-200 text-lg">
        Painel
      </div>

      <!-- Menu -->
      <nav class="flex-1 px-4 py-6 space-y-1">
        <RouterLink
          v-for="item in allowedRoutes"
          :key="item.name"
          :to="{ name: item.name }"
          class="group flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-md text-gray-700 hover:bg-indigo-50 hover:text-indigo-700"
          :class="{ 'bg-indigo-100 text-indigo-700': isActive(item) }"
        >
          <component
            :is="item.icon"
            class="w-5 h-5 flex-shrink-0 transition-colors duration-150"
            :class="{ 'text-indigo-600': isActive(item), 'text-gray-500 group-hover:text-indigo-600': !isActive(item) }"
          />
          <span class="truncate">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <!-- Logout -->
      <button
        @click="logout"
        class="mx-4 mb-6 mt-auto flex items-center gap-3 px-3 py-2 text-sm font-medium text-red-600 border border-red-200 rounded-md hover:bg-red-50 hover:border-red-300 transition-all duration-150"
      >
        <LogOut class="w-5 h-5 flex-shrink-0 text-red-500" />
        <span>Terminar sessão</span>
      </button>
    </aside>

    <!-- Conteúdo principal -->
    <main class="flex-1 p-8 overflow-y-auto">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Home, Users, FileText, LogOut } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const dashboardRoutes = [
  { name: 'DashboardHome', label: 'Início', icon: Home, roles: ['USER', 'ADMIN'] },
  { name: 'Users', label: 'Utilizadores', icon: Users , roles: ['ADMIN'] },
  { name: 'Reports', label: 'Relatórios', icon: FileText, roles: ['ADMIN', 'USER'] },
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
