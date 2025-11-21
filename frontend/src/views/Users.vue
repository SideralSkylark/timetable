<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- Header -->
    <div class="max-w-6xl mx-auto mb-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="bg-blue-900 p-3 rounded-lg">
              <UserIcon class="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">Gestão de Utilizadores</h1>
              <p class="text-gray-500 text-sm">Gerir contas, emails e permissões</p>
            </div>
          </div>

          <button
            v-if="!showCreateForm"
            @click="showCreateForm = true"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition"
          >
            <Plus class="w-5 h-5" />
            Novo Utilizador
          </button>
        </div>
      </div>
    </div>

    <!-- Create Form -->
    <div v-if="showCreateForm" class="max-w-4xl mx-auto mb-8">
      <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <CrudForm
          :fields="createFields"
          title="Criar novo usuário"
          is-create
          @submit="createUser"
          @cancel="showCreateForm = false"
        />
      </div>
    </div>

    <!-- Edit Form -->
    <div v-if="editingUser" class="max-w-4xl mx-auto mb-8">
      <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <CrudForm
          :fields="editFields"
          :data="editingUser"
          title="Editar usuário"
          @submit="updateUser"
          @cancel="editingUser = null"
        />
      </div>
    </div>

    <!-- Tabela -->
    <div class="max-w-6xl mx-auto">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import type { UserResponse } from '@/services/dto/user'
import CrudForm from '@/component/ui/CrudForm.vue'
import CrudTable from '@/component/ui/CrudTable.vue'

import { User as UserIcon, Plus } from 'lucide-vue-next'

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
