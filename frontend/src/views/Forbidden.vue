<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center px-4">
    <div class="w-full max-w-sm text-center">

      <!-- Icon -->
      <div class="flex justify-center mb-6">
        <div class="bg-red-50 p-4 rounded-2xl border border-red-100">
          <ShieldOff class="w-10 h-10 text-red-500" />
        </div>
      </div>

      <!-- Code + message -->
      <p class="text-xs font-semibold text-red-400 uppercase tracking-widest mb-2">Erro 403</p>
      <h1 class="text-2xl font-semibold text-gray-900 mb-3">Acesso negado</h1>
      <p class="text-sm text-gray-400 leading-relaxed mb-8">
        Não tem autorização para aceder a esta página.<br />
        Contacte o administrador se julgar que isto é um erro.
      </p>

      <!-- Action -->
      <RouterLink
        to="/dashboard"
        class="inline-flex items-center gap-2 bg-blue-900 text-white px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-800 transition"
      >
        <LayoutDashboard class="w-4 h-4" />
        Voltar ao painel
      </RouterLink>

    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { ShieldOff, LayoutDashboard } from 'lucide-vue-next'
</script>
