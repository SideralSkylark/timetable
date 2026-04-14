<template>
  <div class="min-h-screen bg-slate-100 flex items-center justify-center px-4">
    <div class="w-full max-w-sm">

      <!-- Brand mark -->
      <div class="flex flex-col items-center mb-8">
        <div class="bg-blue-900 p-3 rounded-md mb-4">
          <GraduationCap class="w-7 h-7 text-white" />
        </div>
        <h1 class="text-xl font-semibold text-gray-900">Bem-vindo</h1>
        <p class="text-sm text-gray-400 mt-1">Inicie sessão para continuar</p>
      </div>

      <!-- Card -->
      <div class="bg-white rounded-[10px] shadow-sm border border-gray-100 p-6">
        <form @submit.prevent="handleLogin" class="space-y-4">

          <!-- Email -->
          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <Mail class="w-3.5 h-3.5" />
              Email
            </label>
            <input
              v-model="email"
              type="email"
              required
              placeholder="O seu email"
              class="w-full px-3 py-2 border border-gray-200 rounded-md text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800 placeholder:text-gray-300"
            />
          </div>

          <!-- Password -->
          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <Lock class="w-3.5 h-3.5" />
              Password
            </label>
            <div class="relative">
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                required
                placeholder="A sua password"
                class="w-full px-3 py-2 pr-9 border border-gray-200 rounded-md text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800 placeholder:text-gray-300"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-300 hover:text-gray-500 transition"
                tabindex="-1"
              >
                <EyeOff v-if="showPassword" class="w-4 h-4" />
                <Eye v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- Error -->
          <div v-if="error"
            class="flex items-center gap-2 bg-red-50 border border-red-100 rounded-md px-3 py-2.5">
            <AlertCircle class="w-3.5 h-3.5 text-red-500 shrink-0" />
            <p class="text-xs text-red-600">{{ error }}</p>
          </div>

          <!-- Submit -->
          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-blue-900 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-800 transition flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed mt-1"
          >
            <Loader2 v-if="loading" class="w-4 h-4 animate-spin" />
            <LogIn v-else class="w-4 h-4" />
            {{ loading ? 'A entrar...' : 'Entrar' }}
          </button>

        </form>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Role } from '@/services'
import { GraduationCap, Mail, Lock, Eye, EyeOff, AlertCircle, Loader2, LogIn } from 'lucide-vue-next'

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

const router = useRouter()
const authStore = useAuthStore()

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    
    // Role-based redirection
    const roles = authStore.user?.roles ?? []
    if (roles.includes(Role.STUDENT) || roles.includes(Role.TEACHER)) {
      router.push({ name: 'MyTimetableView' })
    } else {
      router.push({ name: 'DashboardHome' })
    }
  } catch {
    error.value = 'Credenciais inválidas. Tente novamente.'
  } finally {
    loading.value = false
  }
}
</script>
