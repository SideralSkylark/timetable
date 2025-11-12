<template>
  <div class="min-h-screen flex items-center justify-center">
    <h1 class="text-3xl font-bold">Bem-vindo, {{ name }} ({{ role }})</h1>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const name = ref('')
const role = ref('')

onMounted(() => {
  name.value = localStorage.getItem('user.username') || ''
  role.value = localStorage.getItem('user.roles') || ''
})
</script>

