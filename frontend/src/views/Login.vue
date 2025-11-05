<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full bg-white shadow-md rounded-2xl p-8 sm:p-10">
      <h2 class="text-center text-3xl font-bold text-gray-900 mb-6">
        Entrar
      </h2>

      <form @submit.prevent="handleLogin" class="space-y-5">
        <!-- Email -->
        <div>
          <label for="email" class="sr-only">Email</label>
          <input
            id="email"
            type="email"
            v-model="email"
            placeholder="Email"
            required
            class="block w-full rounded-md border border-gray-300 px-4 py-2 text-gray-900 placeholder-gray-400 focus:border-indigo-500 focus:ring focus:ring-indigo-200 focus:ring-opacity-50 transition"
          />
        </div>

        <!-- Password -->
        <div>
          <label for="password" class="sr-only">Senha</label>
          <input
            id="password"
            type="password"
            v-model="password"
            placeholder="Senha"
            required
            class="block w-full rounded-md border border-gray-300 px-4 py-2 text-gray-900 placeholder-gray-400 focus:border-indigo-500 focus:ring focus:ring-indigo-200 focus:ring-opacity-50 transition"
          />
        </div>

        <!-- Submit -->
        <button
          type="submit"
          :disabled="loading"
          class="w-full rounded-md bg-indigo-600 px-4 py-2 text-white font-semibold shadow hover:bg-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-400 focus:ring-offset-2 disabled:opacity-50 transition"
        >
          {{ loading ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>

      <!-- Error message -->
      <p
        v-if="error"
        class="mt-4 text-center text-sm text-red-500"
      >
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
    router.push('/dashboard')
  } catch (err: any) {
    error.value = err.message || 'Falha ao fazer login'
  } finally {
    loading.value = false
  }
}
</script>
