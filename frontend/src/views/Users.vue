<template>
  <div class="min-h-screen bg-gray-50">

    <!-- Header -->
    <div class="mb-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="bg-blue-900 p-2.5 rounded-lg">
              <UsersIcon class="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 class="text-xl font-semibold text-gray-900">Gestão de utilizadores</h1>
              <p class="text-gray-400 text-sm">Gerir contas, emails e permissões</p>
            </div>
          </div>
          <button @click="openUserModal"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition text-sm font-medium">
            <Plus class="w-4 h-4" />
            Novo utilizador
          </button>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="mb-5">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 px-5 py-4">
        <div class="flex flex-wrap items-end gap-4">

          <!-- Username -->
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Username</label>
            <div class="relative">
              <input
                v-model="filters.username"
                type="text"
                placeholder="Pesquisar utilizador..."
                class="h-8 pl-8 pr-3 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition placeholder:text-gray-300"
                style="width: 180px;"
              />
              <Search class="w-3.5 h-3.5 text-gray-300 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Email -->
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Email</label>
            <div class="relative">
              <input
                v-model="filters.email"
                type="text"
                placeholder="Pesquisar email..."
                class="h-8 pl-8 pr-3 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition placeholder:text-gray-300"
                style="width: 180px;"
              />
              <Mail class="w-3.5 h-3.5 text-gray-300 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Role -->
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Permissão</label>
            <div class="relative">
              <select
                v-model="filters.role"
                class="h-8 px-3 pr-8 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer"
                style="width: 160px;"
              >
                <option value="">Todas</option>
                <option value="ADMIN">ADMIN</option>
                <option value="COORDINATOR">COORDINATOR</option>
                <option value="TEACHER">TEACHER</option>
                <option value="STUDENT">STUDENT</option>
                <option value="USER">USER</option>
              </select>
              <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Teacher type — only shown when role is TEACHER or unfiltered -->
          <Transition name="fade">
            <div v-if="filters.role === '' || filters.role === 'TEACHER'" class="flex flex-col gap-1">
              <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Tipo de docente</label>
              <div class="relative">
                <select
                  v-model="filters.teacherType"
                  class="h-8 px-3 pr-8 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer"
                  style="width: 160px;"
                >
                  <option value="">Todos</option>
                  <option value="FULL_TIME">Tempo inteiro</option>
                  <option value="PART_TIME">Tempo parcial</option>
                </select>
                <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              </div>
            </div>
          </Transition>

          <!-- Clear button -->
          <div class="flex-1 flex items-end justify-end">
            <button
              v-if="activeFilterCount > 0"
              @click="clearFilters"
              class="h-8 flex items-center gap-1.5 px-3 border border-gray-200 text-xs text-gray-500 rounded-lg hover:bg-gray-50 transition"
            >
              <X class="w-3.5 h-3.5" />
              Limpar filtros
              <span class="bg-blue-900 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center font-medium leading-none">
                {{ activeFilterCount }}
              </span>
            </button>
          </div>

        </div>
      </div>
    </div>

    <div>
      <!-- Delete confirmation banner -->
      <div v-if="confirmDeleteId !== null"
        class="mb-3 flex items-center justify-between bg-red-50 border border-red-100 rounded-lg px-4 py-3">
        <span class="text-sm text-red-700">Tem a certeza que quer eliminar este utilizador?</span>
        <div class="flex gap-2">
          <button @click="confirmDeleteId = null"
            class="px-3 py-1.5 text-xs border border-gray-200 text-gray-500 rounded-md hover:bg-white transition">
            Cancelar
          </button>
          <button @click="confirmDelete(confirmDeleteId!)"
            class="px-3 py-1.5 text-xs bg-red-600 text-white rounded-md hover:bg-red-700 transition font-medium">
            Eliminar
          </button>
        </div>
      </div>

      <!-- Table -->
      <CrudTable
        :columns="tableColumns"
        :rows="filteredUsers"
        :currentPage="currentPage"
        :totalPages="pagedUsers?.page.totalPages ?? 0"
        @edit="openEdit"
        @delete="(id: number) => (confirmDeleteId = id)"
        @change-page="fetchUsers"
      >
        <template #empty>
          <UsersIcon class="w-8 h-8 mx-auto mb-3 text-gray-300" />
          <p>{{ activeFilterCount > 0 ? 'Nenhum utilizador corresponde aos filtros' : 'Nenhum utilizador registado' }}</p>
        </template>

        <template #cell-username="{ value }">
          <span class="font-medium text-gray-800">{{ value }}</span>
        </template>

        <template #cell-email="{ value }">
          <span class="text-gray-500">{{ value }}</span>
        </template>

        <template #cell-roles="{ value }">
          <div class="flex flex-wrap gap-1">
            <span
              v-for="role in value"
              :key="role"
              :class="roleBadgeClass(role)"
              class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium">
              {{ role }}
            </span>
          </div>
        </template>
      </CrudTable>
    </div>

    <!-- Modal -->
    <div v-if="showUserModal" class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
      @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-2xl max-w-md w-full max-h-[90vh] overflow-y-auto border border-gray-100">

        <div class="p-5 border-b border-gray-100 flex items-center gap-3">
          <div :class="editingUser ? 'bg-amber-50' : 'bg-blue-50'" class="p-2 rounded-lg">
            <UserPlus v-if="!editingUser" class="w-4 h-4 text-blue-900" />
            <Edit v-else class="w-4 h-4 text-amber-600" />
          </div>
          <div>
            <h2 class="text-base font-semibold text-gray-900">
              {{ editingUser ? 'Editar utilizador' : 'Novo utilizador' }}
            </h2>
            <p v-if="editingUser" class="text-xs text-gray-400 mt-0.5">{{ editingUser.username }}</p>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="p-5 space-y-5">

          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <User class="w-3.5 h-3.5" />
              Username <span class="text-blue-900">*</span>
            </label>
            <input v-model="formData.username" type="text" required
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800 placeholder:text-gray-300"
              placeholder="Digite o username" />
          </div>

          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <Mail class="w-3.5 h-3.5" />
              Email <span class="text-blue-900">*</span>
            </label>
            <input v-model="formData.email" type="email" required
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800 placeholder:text-gray-300"
              placeholder="Digite o email" />
          </div>

          <div v-if="!editingUser">
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <Lock class="w-3.5 h-3.5" />
              Password <span class="text-blue-900">*</span>
            </label>
            <input v-model="formData.password" type="password" required
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800 placeholder:text-gray-300"
              placeholder="Digite a password" />
          </div>

          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-2">
              <Shield class="w-3.5 h-3.5" />
              Permissões <span class="text-blue-900">*</span>
            </label>
            <div class="border border-gray-200 rounded-lg overflow-hidden">
              <div class="flex items-center justify-between px-3 py-2.5 bg-gray-50 border-b border-gray-100">
                <div>
                  <span class="text-sm font-medium text-gray-700">USER</span>
                  <p class="text-xs text-gray-400">Permissão básica (obrigatória)</p>
                </div>
                <div class="w-4 h-4 rounded bg-blue-900 flex items-center justify-center">
                  <Check class="w-2.5 h-2.5 text-white" />
                </div>
              </div>
              <label
                v-for="role in toggleableRoles"
                :key="role.value"
                class="flex items-center justify-between px-3 py-2.5 border-b border-gray-100 last:border-0 cursor-pointer hover:bg-gray-50 transition"
              >
                <div>
                  <span class="text-sm font-medium text-gray-700">{{ role.value }}</span>
                  <p class="text-xs text-gray-400">{{ role.description }}</p>
                </div>
                <input
                  type="checkbox"
                  :value="role.value"
                  v-model="formData.selectedRoles"
                  class="w-4 h-4 text-blue-900 rounded border-gray-300 focus:ring-blue-900"
                />
              </label>
            </div>
            <p class="text-xs text-gray-400 mt-1.5">USER é sempre atribuído por padrão.</p>
          </div>

          <div v-if="formData.selectedRoles.includes('TEACHER')">
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-2">
              <BookOpen class="w-3.5 h-3.5" />
              Tipo de docente <span class="text-blue-900">*</span>
            </label>
            <div class="border border-gray-200 rounded-lg overflow-hidden">
              <label
                v-for="type in teacherTypes"
                :key="type.value"
                class="flex items-center justify-between px-3 py-2.5 border-b border-gray-100 last:border-0 cursor-pointer hover:bg-gray-50 transition"
                :class="formData.teacherType === type.value ? 'bg-blue-50' : ''"
              >
                <div>
                  <span class="text-sm font-medium text-gray-700">{{ type.label }}</span>
                  <p class="text-xs text-gray-400">{{ type.description }}</p>
                </div>
                <input
                  type="radio"
                  :value="type.value"
                  v-model="formData.teacherType"
                  class="w-4 h-4 text-blue-900 border-gray-300 focus:ring-blue-900"
                />
              </label>
            </div>
          </div>

          <div class="flex gap-2 pt-1">
            <button type="button" @click="closeModal"
              class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center justify-center gap-1.5">
              <X class="w-3.5 h-3.5" />
              Cancelar
            </button>
            <button type="submit"
              class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg text-sm hover:bg-blue-800 transition flex items-center justify-center gap-1.5 font-medium">
              <Check class="w-3.5 h-3.5" />
              {{ editingUser ? 'Atualizar utilizador' : 'Criar utilizador' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import type { UserResponse, TeacherType } from '@/services/dto/user'
import CrudTable from '@/component/ui/CrudTable.vue'
import {
  Users as UsersIcon,
  Plus,
  UserPlus,
  Edit,
  User,
  Mail,
  Lock,
  Shield,
  BookOpen,
  X,
  Check,
  Search,
  ChevronDown,
} from 'lucide-vue-next'

const userStore = useUserStore()
const toast = useToast()
const editingUser = ref<UserResponse | null>(null)
const showUserModal = ref(false)
const confirmDeleteId = ref<number | null>(null)
const pagedUsers = computed(() => userStore.pagedUsers)
const currentPage = ref(0)

// ── Hierarquia ────────────────────────────────────────────────────
const ROLE_HIERARCHY: Record<string, number> = {
  USER: 1, STUDENT: 2, TEACHER: 3,
  COORDINATOR: 4, ASISTENT: 5, DIRECTOR: 6, ADMIN: 7,
}

const ALL_ROLES = [
  { value: 'STUDENT',     description: 'Acesso de estudante' },
  { value: 'TEACHER',     description: 'Acesso de professor' },
  { value: 'COORDINATOR', description: 'Acesso de coordenador' },
  { value: 'ASISTENT',    description: 'Acesso de assistente' },
  { value: 'DIRECTOR',    description: 'Acesso de director' },
  { value: 'ADMIN',       description: 'Acesso administrativo completo' },
]

// Prioridade máxima do utilizador autenticado
const myPriority = computed(() => {
  const roles = userStore.currentUser?.roles ?? []
  return Math.max(...roles.map(r => ROLE_HIERARCHY[r] ?? 0), 0)
})

// Roles que pode atribuir (estritamente abaixo de si)
const assignableRoles = computed(() =>
  ALL_ROLES.filter(r => (ROLE_HIERARCHY[r.value] ?? 0) < myPriority.value)
)

// Roles visíveis no filtro (mesma lógica)
const visibleRoles = computed(() =>
  ALL_ROLES.filter(r => (ROLE_HIERARCHY[r.value] ?? 0) < myPriority.value)
)

// Pode gerir um utilizador se a prioridade máxima do target for menor que a sua
const canManageUser = (user: UserResponse): boolean => {
  const targetMax = Math.max(...(user.roles ?? []).map(r => ROLE_HIERARCHY[r] ?? 0), 0)
  return myPriority.value > targetMax
}

// ── Filters ───────────────────────────────────────────────────────
const filters = reactive({
  username: '',
  email: '',
  role: '',
  teacherType: '',
})

// Auto-clear teacher type when switching to a non-TEACHER role
watch(() => filters.role, (val) => {
  if (val !== '' && val !== 'TEACHER') filters.teacherType = ''
})

const activeFilterCount = computed(() => [
  filters.username.trim() !== '',
  filters.email.trim() !== '',
  filters.role !== '',
  filters.teacherType !== '',
].filter(Boolean).length)

const clearFilters = () => {
  filters.username = ''
  filters.email = ''
  filters.role = ''
  filters.teacherType = ''
}

const filteredUsers = computed(() => {
  return (pagedUsers.value?.content ?? []).filter(user => {
    if (filters.username.trim() && !user.username.toLowerCase().includes(filters.username.trim().toLowerCase())) return false
    if (filters.email.trim() && !user.email.toLowerCase().includes(filters.email.trim().toLowerCase())) return false
    if (filters.role !== '' && !user.roles.includes(filters.role)) return false
    if (filters.teacherType !== '' && user.teacherType !== filters.teacherType) return false
    return true
  })
})

// ── Form ──────────────────────────────────────────────────────────
const formData = reactive({
  username: '',
  email: '',
  password: '',
  selectedRoles: [] as string[],
  teacherType: null as TeacherType | null
})

const tableColumns = [
  { key: 'username', label: 'Username' },
  { key: 'email', label: 'Email' },
  { key: 'roles', label: 'Permissões' },
]

const toggleableRoles = [
  { value: 'STUDENT',     description: 'Acesso de estudante' },
  { value: 'TEACHER',     description: 'Acesso de professor' },
  { value: 'COORDINATOR', description: 'Acesso de coordenador' },
  { value: 'ADMIN',       description: 'Acesso administrativo completo' },
]

const teacherTypes = [
  { value: 'FULL_TIME', label: 'Tempo inteiro', description: '16h mínimo, até 24h/semana' },
  { value: 'PART_TIME', label: 'Tempo parcial', description: 'Máximo 8h/semana' },
]

const roleBadgeClass = (role: string) => {
  const map: Record<string, string> = {
    ADMIN:       'bg-blue-900 text-white',
    DIRECTOR:    'bg-purple-100 text-purple-800',
    ASISTENT:    'bg-indigo-100 text-indigo-800',
    COORDINATOR: 'bg-blue-100 text-blue-800',
    TEACHER:     'bg-blue-50 text-blue-700',
    STUDENT:     'bg-gray-100 text-gray-600',
    USER:        'bg-gray-50 text-gray-400',
  }
  return map[role] ?? 'bg-gray-100 text-gray-500'
}

const fetchUsers = async (page = 0) => {
  currentPage.value = page
  await userStore.fetchUsers(page, 10)
}

const handleSubmit = async () => {
  const roles = ['USER', ...formData.selectedRoles]
  const isTeacher = formData.selectedRoles.includes('TEACHER')
  const data = {
    username: formData.username,
    email: formData.email,
    roles,
    ...(formData.password && { password: formData.password }),
    teacherType: isTeacher ? formData.teacherType : null,
  }
  editingUser.value ? await updateUser(data) : await createUser(data)
}

const createUser = async (data: any) => {
  await userStore.createUser(data)
  toast.success('Utilizador criado com sucesso')
  closeModal()
  fetchUsers(currentPage.value)
}

const updateUser = async (data: any) => {
  if (!editingUser.value) return
  await userStore.updateUser(editingUser.value.id, data)
  toast.success('Utilizador atualizado com sucesso')
  closeModal()
  fetchUsers(currentPage.value)
}

const openEdit = (user: UserResponse) => {
  editingUser.value = user
  formData.username = user.username
  formData.email = user.email
  formData.password = ''
  formData.selectedRoles = user.roles.filter(r => r !== 'USER')
  formData.teacherType = user.teacherType ?? null
  showUserModal.value = true
}

const confirmDelete = async (id: number) => {
  await userStore.deleteUser(id)
  confirmDeleteId.value = null
  toast.success('Utilizador removido com sucesso')
  fetchUsers(currentPage.value)
}

const openUserModal = () => {
  editingUser.value = null
  formData.username = ''
  formData.email = ''
  formData.password = ''
  formData.selectedRoles = []
  formData.teacherType = null
  showUserModal.value = true
}

const closeModal = () => {
  showUserModal.value = false
  editingUser.value = null
}

onMounted(fetchUsers)
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
