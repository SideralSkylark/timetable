<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">Gerir Utilizadores</h1>

    <!-- Botão para abrir o formulário de criação -->
    <button
      v-if="!showCreateForm"
      @click="showCreateForm = true"
      class="mb-4 px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
    >
      Criar novo usuário
    </button>

    <!-- Formulário de criação -->
    <CrudForm
      v-if="showCreateForm"
      :fields="createFields"
      title="Criar novo usuário"
      isCreate
      @submit="createUser"
      @cancel="showCreateForm = false"
    />

    <!-- Formulário de edição -->
    <CrudForm
      v-if="editingUser"
      :fields="editFields"
      :data="editingUser"
      title="Editar usuário"
      @submit="updateUser"
      @cancel="editingUser = null"
    />

    <!-- Tabela de usuários -->
    <CrudTable
      :columns="tableColumns"
      :rows="pagedUsers?.content ?? []"
      :currentPage="currentPage"
      :totalPages="pagedUsers?.page.totalPages ?? 0"
      @edit="openEdit"
      @delete="deleteUser"
      @change-page="fetchUsers"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import type { UserResponse } from '@/services/dto/user'
import CrudForm from '@/component/ui/CrudForm.vue'
import CrudTable from '@/component/ui/CrudTable.vue'

const userStore = useUserStore()
const editingUser = ref<UserResponse | null>(null)
const showCreateForm = ref(false)
const pagedUsers = ref(userStore.pagedUsers)
const currentPage = ref(0)

const tableColumns = [
  { key: 'id', label: 'ID' },
  { key: 'username', label: 'Username' },
  { key: 'email', label: 'Email' },
  { key: 'roles', label: 'Roles' },
]

const createFields = [
  { name: 'username', type: 'text', placeholder: 'Username' },
  { name: 'email', type: 'email', placeholder: 'Email' },
  { name: 'password', type: 'password', placeholder: 'Password' },
  { name: 'rolesString', type: 'text', placeholder: 'Roles (comma separated)' },
]

const editFields = [
  { name: 'username', type: 'text', placeholder: 'Username' },
  { name: 'email', type: 'email', placeholder: 'Email' },
  { name: 'rolesString', type: 'text', placeholder: 'Roles (comma separated)' },
]

const fetchUsers = async (page = 0) => {
  currentPage.value = page
  await userStore.fetchUsers(page, 10)
  pagedUsers.value = userStore.pagedUsers
}

const createUser = async (data: any) => {
  if (data.rolesString) {
    data.roles = data.rolesString.split(',').map((r: string) => r.trim())
    delete data.rolesString
  }
  await userStore.createUser(data)
  showCreateForm.value = false
  fetchUsers(currentPage.value)
}

const openEdit = (user: UserResponse) => { 
  editingUser.value = {
    ...user,
    rolesString: user.roles.join(', '),
  } as any 
}

const updateUser = async (data: any) => {
  if (!editingUser.value) return

  if (data.rolesString) {
    data.roles = data.rolesString.split(',').map((r: string) => r.trim())
    delete data.rolesString
  }

  await userStore.updateUser(editingUser.value.id, data)
  editingUser.value = null
  fetchUsers(currentPage.value)
}

const deleteUser = async (id: number) => {
  await userStore.deleteUser(id)
  fetchUsers(currentPage.value)
}

onMounted(fetchUsers)
</script>
