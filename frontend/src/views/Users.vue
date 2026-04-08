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
                              <option v-for="role in visibleRoles" :key="role.value" :value="role.value">
                                {{ roleLabel(role.value) }}
                              </option>
                            </select>              <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Status -->
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Estado</label>
            <div class="relative">
              <select
                v-model="filters.status"
                class="h-8 px-3 pr-8 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer"
                style="width: 140px;"
              >
                <option value="">Todos</option>
                <option value="ACTIVE">Ativo</option>
                <option value="INACTIVE">Inativo</option>
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
        :canEdit="canEdit"
        :canDelete="canDelete"
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
              {{ roleLabel(role) }}
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

        <form @submit.prevent="handleSubmit" novalidate class="p-5 space-y-5">

          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <User class="w-3.5 h-3.5" />
              Username <span class="text-blue-900">*</span>
            </label>
            <input v-model="formData.username" type="text"
              class="w-full px-3 py-2 border rounded-lg text-sm outline-none transition text-gray-800 placeholder:text-gray-300"
              :class="formErrors.username ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'"
              placeholder="Digite o username" />
            <p v-if="formErrors.username" class="text-red-500 text-[10px] mt-1">O username é obrigatório</p>
          </div>

          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <Mail class="w-3.5 h-3.5" />
              Email <span class="text-blue-900">*</span>
            </label>
            <input v-model="formData.email" type="email"
              class="w-full px-3 py-2 border rounded-lg text-sm outline-none transition text-gray-800 placeholder:text-gray-300"
              :class="formErrors.email ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'"
              placeholder="Digite o email" />
            <p v-if="formErrors.email" class="text-red-500 text-[10px] mt-1">Introduza um email válido</p>
          </div>

          <div v-if="!editingUser">
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <Lock class="w-3.5 h-3.5" />
              Password <span class="text-blue-900">*</span>
            </label>
            <input v-model="formData.password" type="password"
              class="w-full px-3 py-2 border rounded-lg text-sm outline-none transition text-gray-800 placeholder:text-gray-300"
              :class="formErrors.password ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'"
              placeholder="Digite a password" />
            <p v-if="formErrors.password" class="text-red-500 text-[10px] mt-1">Password deve ter entre 8 e 100 caracteres</p>
          </div>

          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-2">
              <Shield class="w-3.5 h-3.5" />
              Permissões <span class="text-blue-900">*</span>
            </label>
            <div class="border border-gray-200 rounded-lg overflow-hidden">
              <div class="flex items-center justify-between px-3 py-2.5 bg-gray-50 border-b border-gray-100">
                <div>
                  <span class="text-sm font-medium text-gray-700">Utilizador</span>
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
                  <span class="text-sm font-medium text-gray-700">{{ roleLabel(role.value) }}</span>
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
            <p class="text-xs text-gray-400 mt-1.5">Utilizador é sempre atribuído por padrão.</p>
          </div>

          <div v-if="formData.selectedRoles.includes('TEACHER')">
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-2">
              <BookOpen class="w-3.5 h-3.5" />
              Tipo de docente <span class="text-blue-900">*</span>
            </label>
            <div class="border rounded-lg overflow-hidden"
              :class="formErrors.teacherType ? 'border-red-500' : 'border-gray-200'">
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
            <p v-if="formErrors.teacherType" class="text-red-500 text-[10px] mt-1">O tipo de docente é obrigatório para professores</p>
          </div>

          <!-- Reset Password (Admin only, edit mode) -->
          <div v-if="editingUser && isAdmin" class="pt-2 border-t border-gray-100">
            <button
              type="button"
              @click="handleResetPassword"
              class="w-full h-9 flex items-center justify-center gap-2 text-xs font-medium text-amber-700 bg-amber-50 border border-amber-100 rounded-lg hover:bg-amber-100 transition"
            >
              <KeyRound class="w-3.5 h-3.5" />
              Repor password
            </button>
            <p class="text-[10px] text-gray-400 mt-1 text-center">
              Gera uma nova password aleatória e remove a actual.
            </p>
          </div>

          <div class="flex gap-2 pt-1">
            <button type="button" @click="closeModal"
              class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center justify-center gap-1.5">
              <X class="w-3.5 h-3.5" />
              Cancelar
            </button>
            <button type="submit"
              class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg text-sm hover:bg-blue-800 transition flex items-center justify-center gap-1.5 font-medium"
            >
              <Check class="w-3.5 h-3.5" />
              {{ editingUser ? 'Atualizar utilizador' : 'Criar utilizador' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal: Reset Password Success -->
    <div v-if="resetSuccessModal" class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-[60]"
      @click.self="resetSuccessModal = false">
      <div class="bg-white rounded-xl shadow-2xl max-w-sm w-full border border-gray-100 p-6 print-container">
        
        <div class="flex flex-col items-center text-center space-y-4">
          <div class="bg-amber-50 p-3 rounded-full">
            <KeyRound class="w-6 h-6 text-amber-600" />
          </div>
          
          <div>
            <h2 class="text-lg font-bold text-gray-900">Password reposta!</h2>
            <div class="mt-2 flex items-start gap-2 bg-red-50 border border-red-100 rounded-lg px-3 py-2 text-red-700">
              <AlertCircle class="w-4 h-4 mt-0.5 shrink-0" />
              <p class="text-xs font-medium no-print">
                Esta password só é mostrada uma vez. Guarde-a num local seguro.
              </p>
            </div>
          </div>

          <div class="w-full bg-gray-50 border-2 border-dashed border-gray-200 rounded-xl p-4 my-2 relative group">
            <span class="text-2xl font-mono font-bold tracking-wider text-blue-900 select-all print-only-text">
              {{ tempPassword }}
            </span>
          </div>

          <div class="grid grid-cols-2 gap-3 w-full no-print">
            <button
              @click="copyToClipboard"
              class="h-10 flex items-center justify-center gap-2 px-4 border border-gray-200 rounded-lg text-sm font-medium hover:bg-gray-50 transition"
              :class="copyFeedback ? 'text-green-600 border-green-200 bg-green-50' : 'text-gray-600'"
            >
              <Copy v-if="!copyFeedback" class="w-4 h-4" />
              <Check v-else class="w-4 h-4" />
              {{ copyFeedback ? 'Copiado!' : 'Copiar' }}
            </button>
            <button
              @click="printPassword"
              class="h-10 flex items-center justify-center gap-2 px-4 border border-gray-200 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-50 transition"
            >
              <Printer class="w-4 h-4" />
              Imprimir
            </button>
          </div>

          <button
            @click="resetSuccessModal = false"
            class="w-full h-10 bg-blue-900 text-white rounded-lg text-sm font-semibold hover:bg-blue-800 transition no-print"
          >
            Fechar
          </button>
        </div>
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
  KeyRound,
  Copy,
  Printer,
  AlertCircle,
} from 'lucide-vue-next'

const userStore = useUserStore()
const toast = useToast()

// ── Role Helpers ──────────────────────────────────────────────────
const isAdmin = computed(() => userStore.myHighestRole === 'ADMIN')
const isDirector = computed(() => userStore.myHighestRole === 'DIRECTOR')
const isAssistant = computed(() => userStore.myHighestRole === 'ASISTENT')

const editingUser = ref<UserResponse | null>(null)
const showUserModal = ref(false)
const resetSuccessModal = ref(false)
const tempPassword = ref('')
const copyFeedback = ref(false)
const confirmDeleteId = ref<number | null>(null)
const pagedUsers = computed(() => userStore.pagedUsers)
const currentPage = ref(0)

const ALL_ROLES_INFO = [
  { value: 'STUDENT',     description: 'Acesso de estudante' },
  { value: 'TEACHER',     description: 'Acesso de professor' },
  { value: 'COORDINATOR', description: 'Acesso de coordenador' },
  { value: 'ASISTENT',    description: 'Acesso de assistente' },
  { value: 'DIRECTOR',    description: 'Acesso de director' },
  { value: 'ADMIN',       description: 'Acesso administrativo completo' },
]

const toggleableRoles = computed(() => {
  if (isAdmin.value) return ALL_ROLES_INFO
  if (isDirector.value) return ALL_ROLES_INFO.filter(r => r.value !== 'ADMIN' && r.value !== 'DIRECTOR')
  if (isAssistant.value) return ALL_ROLES_INFO.filter(r => r.value !== 'ADMIN' && r.value !== 'DIRECTOR' && r.value !== 'ASISTENT')
  return []
})

// Roles que pode atribuir (estritamente abaixo de si)
const assignableRoles = toggleableRoles

// Roles visíveis no filtro (sem restrição)
const visibleRoles = ALL_ROLES_INFO

// ── Filters ───────────────────────────────────────────────────────
const filters = reactive({
  username: '',
  email: '',
  role: '',
  status: '',
  teacherType: '',
})

// Auto-clear teacher type when switching to a non-TEACHER role
watch(() => filters.role, (val) => {
  if (val !== '' && val !== 'TEACHER') filters.teacherType = ''
})

// Refetch users when backend filters change
watch([() => filters.username, () => filters.email, () => filters.role, () => filters.status, () =>
filters.teacherType], () => {
  fetchUsers(0) // Reset to first page when filters change
})

const activeFilterCount = computed(() => [
  filters.username.trim() !== '',
  filters.email.trim() !== '',
  filters.role !== '',
  filters.status !== '',
  filters.teacherType !== '',
].filter(Boolean).length)

const clearFilters = () => {
  filters.username = ''
  filters.email = ''
  filters.role = ''
  filters.status = ''
  filters.teacherType = ''
}

const filteredUsers = computed(() => pagedUsers.value?.content ?? [])

// ── Form ──────────────────────────────────────────────────────────
const formData = reactive({
  username: '',
  email: '',
  password: '',
  selectedRoles: [] as string[],
  teacherType: null as TeacherType | null
})

const formErrors = reactive({
  username: false,
  email: false,
  password: false,
  teacherType: false
})

const tableColumns = [
  { key: 'username', label: 'Username' },
  { key: 'email', label: 'Email' },
  { key: 'roles', label: 'Permissões' },
]

const canEdit = (user: UserResponse) => {
  if (isAdmin.value) return true
  const isSelf = user.id === userStore.currentUser?.id
  const targetRoles = user.roles || []
  const isTargetAdmin = targetRoles.includes('ADMIN')
  const isTargetDirector = targetRoles.includes('DIRECTOR')
  const isTargetAssistant = targetRoles.includes('ASISTENT')

  if (isDirector.value) {
    return !isTargetAdmin && (!isTargetDirector || isSelf)
  }
  if (isAssistant.value) {
    return !isTargetAdmin && !isTargetDirector && (!isTargetAssistant || isSelf)
  }
  return false
}

const canDelete = (user: UserResponse) => {
  if (isAdmin.value) return true
  
  const isSelf = user.id === userStore.currentUser?.id
  const targetRoles = user.roles || []
  const isTargetAdmin = targetRoles.includes('ADMIN')
  const isTargetDirector = targetRoles.includes('DIRECTOR')
  const isTargetAssistant = targetRoles.includes('ASISTENT')

  if (isDirector.value) {
    return !isTargetAdmin && !isTargetDirector && !isSelf
  }
  if (isAssistant.value) {
    return !isTargetAdmin && !isTargetDirector && !isTargetAssistant && !isSelf
  }
  return false
}

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

const roleLabel = (role: string) => {
  const labels: Record<string, string> = {
    ADMIN: 'Administrador',
    DIRECTOR: 'Diretor',
    ASISTENT: 'Assistente',
    COORDINATOR: 'Coordenador',
    TEACHER: 'Professor',
    STUDENT: 'Estudante',
    USER: 'Utilizador',
  }
  return labels[role] ?? role
}

const fetchUsers = async (page = 0) => {
  currentPage.value = page
  const backendFilters = {
    username: filters.username.trim() || undefined,
    email: filters.email.trim() || undefined,
    role: filters.role || undefined,
    status: filters.status || undefined,
    teacherType: filters.teacherType || undefined,
  }
  await userStore.fetchUsers(page, 10, backendFilters)
}

const handleSubmit = async () => {
  formErrors.username = !formData.username?.trim()
  formErrors.email = !formData.email?.trim() || !/^\S+@\S+\.\S+$/.test(formData.email)
  formErrors.password = !editingUser.value && (formData.password.length < 8 || formData.password.length > 100)
  formErrors.teacherType = formData.selectedRoles.includes('TEACHER') && !formData.teacherType

  if (formErrors.username || formErrors.email || formErrors.password || formErrors.teacherType) {
    toast.error('Por favor, preencha todos os campos obrigatórios corretamente.')
    return
  }

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

const handleResetPassword = async () => {
  if (!editingUser.value) return
  try {
    const password = await userStore.resetPassword(editingUser.value.id)
    tempPassword.value = password
    resetSuccessModal.value = true
    closeModal()
  } catch (err: any) {
    toast.error(err.message || 'Erro ao repor password')
  }
}

const copyToClipboard = async () => {
  try {
    await navigator.clipboard.writeText(tempPassword.value)
    copyFeedback.value = true
    setTimeout(() => {
      copyFeedback.value = false
    }, 2000)
  } catch {
    toast.error('Erro ao copiar password')
  }
}

const printPassword = () => {
  window.print()
}

const openEdit = (user: UserResponse) => {
  editingUser.value = user
  formData.username = user.username
  formData.email = user.email
  formData.password = ''
  formData.selectedRoles = user.roles.filter(r => r !== 'USER')
  formData.teacherType = user.teacherType ?? null
  
  formErrors.username = false
  formErrors.email = false
  formErrors.password = false
  formErrors.teacherType = false
  
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

  formErrors.username = false
  formErrors.email = false
  formErrors.password = false
  formErrors.teacherType = false

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

@media print {
  body * {
    visibility: hidden;
  }
  .print-container, .print-container * {
    visibility: visible;
  }
  .print-container {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    border: none !important;
    box-shadow: none !important;
  }
  .no-print {
    display: none !important;
  }
  .print-only-text {
    font-size: 48pt !important;
    display: block !important;
    text-align: center !important;
    margin-top: 2in !important;
  }
}
</style>
