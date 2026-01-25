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
            @click="openUserModal"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition"
          >
            <Plus class="w-5 h-5" />
            Novo Utilizador
          </button>
        </div>
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

    <!-- Modal Utilizador -->
    <div
      v-if="showUserModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
    >
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
        <!-- Header -->
        <div class="border-b border-gray-200 p-6">
          <div class="flex items-center gap-3">
            <div :class="editingUser ? 'bg-amber-100' : 'bg-blue-100'" class="p-2 rounded-lg">
              <UserPlusIcon v-if="!editingUser" class="w-5 h-5 text-blue-600" />
              <Edit v-else class="w-5 h-5 text-amber-600" />
            </div>
            <h2 class="text-xl font-semibold text-gray-900">
              {{ editingUser ? `Editar Utilizador` : 'Novo Utilizador' }}
            </h2>
          </div>
          <p v-if="editingUser" class="text-sm text-gray-500 mt-2 ml-11">{{ editingUser.username }}</p>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="p-6 space-y-5">
          <!-- Username -->
          <div>
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <User class="w-4 h-4 text-gray-500" />
              Username *
            </label>
            <input
              v-model="formData.username"
              type="text"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Digite o username"
            />
          </div>

          <!-- Email -->
          <div>
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <Mail class="w-4 h-4 text-gray-500" />
              Email *
            </label>
            <input
              v-model="formData.email"
              type="email"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Digite o email"
            />
          </div>

          <!-- Password (apenas na criação) -->
          <div v-if="!editingUser">
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <Lock class="w-4 h-4 text-gray-500" />
              Password *
            </label>
            <input
              v-model="formData.password"
              type="password"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Digite a password"
            />
          </div>

          <!-- Roles -->
          <div>
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-3">
              <Shield class="w-4 h-4 text-gray-500" />
              Permissões *
            </label>
            <div class="space-y-2 bg-gray-50 p-4 rounded-lg border border-gray-200">
              <!-- USER - sempre selecionado e disabled -->
              <label class="flex items-center gap-3 cursor-not-allowed opacity-75">
                <input
                  type="checkbox"
                  checked
                  disabled
                  class="w-4 h-4 text-blue-600 rounded"
                />
                <div class="flex items-center gap-2 flex-1">
                  <div>
                    <span class="font-medium text-gray-900">USER</span>
                    <p class="text-xs text-gray-500">Permissão básica (obrigatória)</p>
                  </div>
                </div>
              </label>

              <!-- STUDENT -->
              <label class="flex items-center gap-3 cursor-pointer hover:bg-gray-100 p-2 rounded transition">
                <input
                  type="checkbox"
                  value="STUDENT"
                  v-model="formData.selectedRoles"
                  class="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                />
                <div class="flex items-center gap-2 flex-1">
                  <div>
                    <span class="font-medium text-gray-900">STUDENT</span>
                    <p class="text-xs text-gray-500">Acesso de estudante</p>
                  </div>
                </div>
              </label>

              <!-- TEACHER -->
              <label class="flex items-center gap-3 cursor-pointer hover:bg-gray-100 p-2 rounded transition">
                <input
                  type="checkbox"
                  value="TEACHER"
                  v-model="formData.selectedRoles"
                  class="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                />
                <div class="flex items-center gap-2 flex-1">
                  <div>
                    <span class="font-medium text-gray-900">TEACHER</span>
                    <p class="text-xs text-gray-500">Acesso de professor</p>
                  </div>
                </div>
              </label>

              <!-- COORDINATOR -->
              <label class="flex items-center gap-3 cursor-pointer hover:bg-gray-100 p-2 rounded transition">
                <input
                  type="checkbox"
                  value="COORDINATOR"
                  v-model="formData.selectedRoles"
                  class="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                />
                <div class="flex items-center gap-2 flex-1">
                  <div>
                    <span class="font-medium text-gray-900">COORDINATOR</span>
                    <p class="text-xs text-gray-500">Acesso de coordenador</p>
                  </div>
                </div>
              </label>

              <!-- ADMIN -->
              <label class="flex items-center gap-3 cursor-pointer hover:bg-gray-100 p-2 rounded transition">
                <input
                  type="checkbox"
                  value="ADMIN"
                  v-model="formData.selectedRoles"
                  class="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                />
                <div class="flex items-center gap-2 flex-1">
                  <div>
                    <span class="font-medium text-gray-900">ADMIN</span>
                    <p class="text-xs text-gray-500">Acesso administrativo completo</p>
                  </div>
                </div>
              </label>
            </div>
            <p class="text-xs text-gray-500 mt-2">
              * USER é sempre atribuído por padrão
            </p>
          </div>

          <!-- Botões -->
          <div class="flex gap-3 pt-4">
            <button
              type="button"
              @click="closeModal"
              class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition flex items-center justify-center gap-2"
            >
              <X class="w-4 h-4" />
              Cancelar
            </button>
            <button
              type="submit"
              class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg hover:bg-blue-800 transition flex items-center justify-center gap-2"
            >
              <Check class="w-4 h-4" />
              {{ editingUser ? 'Atualizar' : 'Criar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import type { UserResponse } from '@/services/dto/user'
import CrudTable from '@/component/ui/CrudTable.vue'
import { 
  User as UserIcon, 
  Plus, 
  UserPlus as UserPlusIcon,
  Edit,
  User,
  Mail,
  Lock,
  Shield,
  X,
  Check
} from 'lucide-vue-next'

const userStore = useUserStore()
const toast = useToast()
const editingUser = ref<UserResponse | null>(null)
const showUserModal = ref(false)
const pagedUsers = ref(userStore.pagedUsers)
const currentPage = ref(0)

const formData = reactive({
  username: '',
  email: '',
  password: '',
  selectedRoles: [] as string[]
})

const tableColumns = [
  { key: 'username', label: 'Username' },
  { key: 'email', label: 'Email' },
  { key: 'roles', label: 'Roles' },
]

const fetchUsers = async (page = 0) => {
  currentPage.value = page
  await userStore.fetchUsers(page, 10)
  pagedUsers.value = userStore.pagedUsers
}

const handleSubmit = async () => {
  const roles = ['USER', ...formData.selectedRoles]
  
  const data = {
    username: formData.username,
    email: formData.email,
    roles: roles,
    ...(formData.password && { password: formData.password })
  }

  if (editingUser.value) {
    await updateUser(data)
  } else {
    await createUser(data)
  }
}

const createUser = async (data: any) => {
  await userStore.createUser(data)
  toast.success('Usuario criado com sucesso')
  closeModal()
  fetchUsers(currentPage.value)
}

const updateUser = async (data: any) => {
  if (!editingUser.value) return
  await userStore.updateUser(editingUser.value.id, data)
  toast.success('Usuario atualizado com sucesso')
  closeModal()
  fetchUsers(currentPage.value)
}

const openEdit = (user: UserResponse) => {
  editingUser.value = user
  
  // Preencher o formulário
  formData.username = user.username
  formData.email = user.email
  formData.password = ''
  
  // Filtrar USER dos roles selecionáveis (já que é sempre incluído)
  formData.selectedRoles = user.roles.filter(r => r !== 'USER')
  
  showUserModal.value = true
}

const deleteUser = async (id: number) => {
  if (confirm('Tem certeza que deseja excluir este utilizador?')) {
    await userStore.deleteUser(id)
    toast.success('Usuario removido com sucesso')
    fetchUsers(currentPage.value)
  }
}

const openUserModal = () => {
  editingUser.value = null
  formData.username = ''
  formData.email = ''
  formData.password = ''
  formData.selectedRoles = []
  showUserModal.value = true
}

const closeModal = () => {
  showUserModal.value = false
  editingUser.value = null
}

onMounted(fetchUsers)
</script>
