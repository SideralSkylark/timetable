<template>
  <div class="flex flex-col items-center justify-center h-screen text-center">
    <h1 class="text-5xl font-bold mb-4 text-red-600">403</h1>
    <p class="text-lg mb-6">Não tem autorização para aceder a este recurso.</p>
    <RouterLink
      to="/dashboard"
      class="text-blue-500 underline hover:text-blue-600"
    >
      Voltar ao início
    </RouterLink>
  </div>
</template>
