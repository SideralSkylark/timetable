<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="p-10 bg-white shadow-lg rounded-lg w-96">
      <h2 class="text-2xl font-bold mb-6 text-center">Login</h2>

      <form @submit.prevent="handleLogin">
        <input
          v-model="email"
          type="email"
          placeholder="Email"
          class="w-full p-2 mb-4 border rounded"
          required
        />

        <input
          v-model="password"
          type="password"
          placeholder="Senha"
          class="w-full p-2 mb-6 border rounded"
          required
        />

        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded disabled:opacity-50"
        >
          {{ loading ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>

      <p v-if="error" class="text-red-500 text-sm mt-4 text-center">
        {{ error }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const router = useRouter()
const authStore = useAuthStore()

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    router.push('/dashboard') // ou a rota que quiser
  } catch (err: any) {
    error.value = err.message || 'Falha ao fazer login'
  } finally {
    loading.value = false
  }
}
</script>
