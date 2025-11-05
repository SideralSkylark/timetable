<template>
  <div class="flex h-screen">
    <!-- Sidebar -->
    <aside class="w-60 bg-gray-900 text-gray-100 flex flex-col">
      <div class="p-4 font-semibold border-b border-gray-700">Painel</div>

      <nav class="flex-1 p-3 space-y-2">
        <RouterLink
          v-for="child in allowedRoutes"
          :key="child.name"
          :to="`/dashboard/${child.path}`"
          class="block px-3 py-2 rounded hover:bg-gray-800"
          :class="{ 'bg-gray-800': route.name === child.name }"
        >
          {{ child.meta?.label }}
        </RouterLink>
      </nav>

      <button
        class="p-4 text-left border-t border-gray-700 hover:bg-gray-800"
        @click="logout"
      >
        Terminar sessão
      </button>
    </aside>

    <!-- Conteúdo -->
    <main class="flex-1 bg-gray-50 p-6 overflow-y-auto">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const route = useRoute()
const routerInstance = useRouter()
const auth = useAuthStore()

// Obtém as rotas-filhas registadas no router (children de /dashboard)
const dashboardRoute = router.getRoutes().find((r) => r.path === '/dashboard')
const children = dashboardRoute?.children ?? []

// Filtra pelas roles do utilizador
const allowedRoutes = computed(() => {
  const roles = auth.user?.roles ?? []
  return children.filter((r) => {
    if (!r.meta?.roles) return true
    return (r.meta.roles as string[]).some((role) => roles.includes(role))
  })
})

const logout = async () => {
  await auth.logout()
  routerInstance.push({ name: 'Login' })
}
</script>
