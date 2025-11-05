<template>
  <main class="grid min-h-full place-items-center bg-white px-6 py-24 sm:py-32 lg:px-8">
    <div class="text-center">
      <p class="text-4xl font-semibold text-indigo-600">404</p>
      <h1 class="mt-4 text-4xl font-semibold tracking-tight text-gray-900 sm:text-5xl">
        Página não encontrada
      </h1>
      <p class="mt-6 text-lg font-medium text-gray-500 sm:text-xl">
        Desculpe, não conseguimos encontrar a página que procurava.
      </p>
      <div class="mt-10 flex items-center justify-center gap-x-6">
        <RouterLink
          to="/"
          class="rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
        >
          Voltar à página inicial
        </RouterLink>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>
