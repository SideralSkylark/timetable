<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">Gerir Utilizadores</h1>

    <!-- Formulário de criação -->
    <div class="mb-6 p-4 border rounded-md bg-white shadow-sm">
      <h2 class="text-lg font-semibold mb-2">Criar novo usuário</h2>
      <form @submit.prevent="createUser">
        <input
          v-model="newUser.username"
          type="text"
          placeholder="Username"
          class="border rounded px-2 py-1 mr-2"
        />
        <input
          v-model="newUser.email"
          type="email"
          placeholder="Email"
          class="border rounded px-2 py-1 mr-2"
        />
        <input
          v-model="newUser.password"
          type="password"
          placeholder="Password"
          class="border rounded px-2 py-1 mr-2"
        />
        <input
          v-model="newUser.roles"
          type="text"
          placeholder="Roles (comma separated)"
          class="border rounded px-2 py-1 mr-2"
        />
        <button
          type="submit"
          class="px-4 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700"
        >
          Criar
        </button>
      </form>
    </div>

    <!-- Adicione acima do table -->
<div v-if="editingUser" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
  <div class="bg-white p-6 rounded shadow-lg w-96">
    <h2 class="text-lg font-semibold mb-4">Editar usuário</h2>
    <form @submit.prevent="updateUser">
      <input v-model="editingUser.username" type="text" placeholder="Username" class="border rounded px-2 py-1 mb-2 w-full" />
      <input v-model="editingUser.email" type="email" placeholder="Email" class="border rounded px-2 py-1 mb-2 w-full" />
      <input v-model="editingUser.rolesString" type="text" placeholder="Roles (comma separated)" class="border rounded px-2 py-1 mb-4 w-full" />
      <div class="flex justify-end gap-2">
        <button type="button" @click="editingUser = null" class="px-4 py-1 bg-gray-300 rounded hover:bg-gray-400">Cancelar</button>
        <button type="submit" class="px-4 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700">Salvar</button>
      </div>
    </form>
  </div>
</div>


    <!-- Lista de usuários -->
    <div class="bg-white shadow rounded-md overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-2 text-left text-sm font-medium text-gray-500">ID</th>
            <th class="px-4 py-2 text-left text-sm font-medium text-gray-500">Username</th>
            <th class="px-4 py-2 text-left text-sm font-medium text-gray-500">Email</th>
            <th class="px-4 py-2 text-left text-sm font-medium text-gray-500">Roles</th>
            <th class="px-4 py-2 text-left text-sm font-medium text-gray-500">Ações</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="user in pagedUsers?.content" :key="user.id">
            <td class="px-4 py-2">{{ user.id }}</td>
            <td class="px-4 py-2">{{ user.username }}</td>
            <td class="px-4 py-2">{{ user.email }}</td>
            <td class="px-4 py-2">{{ user.roles.join(', ') }}</td>
            <td class="px-4 py-2">
              <button @click="openEdit(user)" class="px-2 py-1 text-white bg-blue-600 rounded hover:bg-blue-700">Editar</button>
              <button
                @click="deleteUser(user.id)"
                class="px-2 py-1 text-white bg-red-600 rounded hover:bg-red-700"
              >
                Apagar
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Paginação -->
      <Pagination
        :page="currentPage"
        :totalPages="pagedUsers?.page.totalPages ?? 0"
        @update:page="fetchUsers"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import type { UserResponse } from '@/services/dto/user'
import Pagination from '@/component/ui/Pagination.vue'

const userStore = useUserStore()

const newUser = reactive({
  username: '',
  email: '',
  password: '',
  roles: '',
})

const editingUser = ref<{ id: number; username: string; email: string; rolesString: string } | null>(null)
const pagedUsers = ref(userStore.pagedUsers)
const currentPage = ref(0)

const fetchUsers = async (page = 0) => {
  currentPage.value = page
  await userStore.fetchUsers(page, 10)
  pagedUsers.value = userStore.pagedUsers
}

const createUser = async () => {
  if (!newUser.username || !newUser.email || !newUser.password) return
  await userStore.createUser({
    username: newUser.username,
    email: newUser.email,
    password: newUser.password,
    roles: newUser.roles.split(',').map(r => r.trim()),
  })
  newUser.username = ''
  newUser.email = ''
  newUser.password = ''
  newUser.roles = ''
  fetchUsers(currentPage.value)
}

const openEdit = (user: UserResponse) => {
  editingUser.value = {
    id: user.id,
    username: user.username,
    email: user.email,
    rolesString: user.roles.join(', ')
  }
}

const updateUser = async () => {
  if (!editingUser.value) return
  await userStore.updateUser(editingUser.value.id, {
    username: editingUser.value.username,
    email: editingUser.value.email,
    roles: editingUser.value.rolesString.split(',').map(r => r.trim())
  })
  editingUser.value = null
  fetchUsers(currentPage.value)
}

const deleteUser = async (id: number) => {
  await userStore.deleteUser(id)
  fetchUsers(currentPage.value)
}

onMounted(() => {
  fetchUsers()
})
</script>
