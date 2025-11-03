<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="p-10 bg-white shadow-lg rounded-lg w-96">
      <h2 class="text-2xl font-bold mb-6">Login</h2>

      <input
        v-model="name"
        placeholder="Nome"
        class="w-full p-2 mb-4 border rounded"
      />

      <select v-model="role" class="w-full p-2 mb-4 border rounded">
        <option value="">Selecione o papel</option>
        <option value="coordinator">Coordenador</option>
        <option value="teacher">Docente</option>
        <option value="student">Estudante</option>
      </select>

      <button
        @click="login"
        class="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded"
      >
        Entrar
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const name = ref('')
const role = ref('')
const router = useRouter()

function login() {
  if (!name.value || !role.value) {
    alert('Preencha todos os campos')
    return
  }

  localStorage.setItem('userName', name.value)
  localStorage.setItem('userRole', role.value)

  router.push('/dashboard') // precisa da rota criada
}
</script>

