<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- Header -->
    <div class="max-w-6xl mx-auto mb-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="bg-blue-900 p-3 rounded-lg">
              <Building class="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">Gestão de Salas</h1>
              <p class="text-gray-500 text-sm">Gerir as salas da instituição</p>
            </div>
          </div>

          <button
            @click="openRoomModal"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition"
          >
            <Plus class="w-5 h-5" />
            Nova Sala
          </button>
        </div>
      </div>
    </div>

    <!-- Tabela -->
    <div class="max-w-6xl mx-auto">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <CrudTable
          :columns="tableColumns"
          :rows="pagedRooms?.content ?? []"
          :currentPage="currentPage"
          :totalPages="pagedRooms?.page.totalPages ?? 0"
          @edit="openEditRoomModal"
          @delete="deleteRoom"
          @change-page="fetchRooms"
        />
      </div>
    </div>

    <!-- Modal Sala -->
    <div
      v-if="showRoomModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
    >
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
        <!-- Header -->
        <div class="border-b border-gray-200 p-6">
          <div class="flex items-center gap-3">
            <div :class="editingRoom ? 'bg-amber-100' : 'bg-blue-100'" class="p-2 rounded-lg">
              <DoorOpen v-if="!editingRoom" class="w-5 h-5 text-blue-600" />
              <Edit v-else class="w-5 h-5 text-amber-600" />
            </div>
            <h2 class="text-xl font-semibold text-gray-900">
              {{ editingRoom ? `Editar Sala` : 'Nova Sala' }}
            </h2>
          </div>
          <p v-if="editingRoom" class="text-sm text-gray-500 mt-2 ml-11">{{ editingRoom.name }}</p>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="p-6 space-y-5">
          <!-- Nome -->
          <div>
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <Tag class="w-4 h-4 text-gray-500" />
              Nome da Sala *
            </label>
            <input
              v-model="formData.name"
              type="text"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Ex: Sala A101, Laboratório 3..."
            />
          </div>

          <!-- Capacidade -->
          <div>
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <Users class="w-4 h-4 text-gray-500" />
              Capacidade *
            </label>
            <input
              v-model.number="formData.capacity"
              type="number"
              required
              min="1"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Número de lugares"
            />
          </div>

          <!-- Curso Atribuído -->
          <div>
            <label class="flex items-center gap-2 text-sm font-medium text-gray-700 mb-1">
              <BookMarked class="w-4 h-4 text-gray-500" />
              Atribuído ao Curso *
            </label>
            <div class="relative">
              <select
                v-model="formData.restrictedToCourseId"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none bg-white"
              >
                <option value="" disabled>Selecione um curso</option>
                <option
                  v-for="course in courseStore.courses"
                  :key="course.id"
                  :value="course.id"
                >
                  {{ course.name }}
                </option>
              </select>
              <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
            </div>
            <p class="text-xs text-gray-500 mt-1">
              A sala será exclusiva para este curso
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
              {{ editingRoom ? 'Atualizar' : 'Criar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoomStore } from '@/stores/room'
import { useCourseStore } from '@/stores/course'
import { useToast } from '@/composables/useToast'
import type { RoomResponse } from '@/services/dto/room'
import CrudTable from '@/component/ui/CrudTable.vue'

import { 
  Building, 
  Plus,
  DoorOpen,
  Edit,
  Tag,
  Users,
  BookMarked,
  ChevronDown,
  X,
  Check
} from 'lucide-vue-next'

const roomStore = useRoomStore()
const courseStore = useCourseStore()
const toast = useToast()
const editingRoom = ref<RoomResponse | null>(null)
const showRoomModal = ref(false)
const pagedRooms = computed(() => roomStore.pagedRooms)
const currentPage = ref(0)

const formData = reactive({
  name: '',
  capacity: null as number | null,
  restrictedToCourseId: '' as string | number
})

const tableColumns = [
  { key: 'name', label: 'Nome' },
  { key: 'capacity', label: 'Capacidade' },
  { key: 'restrictedToCourseName', label: 'Atribuído'},
]

const fetchRooms = async (page = 0) => {
  currentPage.value = page
  await roomStore.fetchRooms(page, 10)
}

const handleSubmit = async () => {
  const data = {
    name: formData.name,
    capacity: formData.capacity,
    restrictedToCourseId: formData.restrictedToCourseId
  }

  if (editingRoom.value) {
    await updateRoom(data)
  } else {
    await createRoom(data)
  }
}

const createRoom = async (data: any) => {
  await roomStore.createRoom(data)
  closeModal()
  toast.success('Sala criada com sucesso!')
  fetchRooms(currentPage.value)
}

const openEditRoomModal = async (room: RoomResponse) => {
  await courseStore.fetchAllCoursesSimple()
  editingRoom.value = { ...room }
  
  // Preencher o formulário
  formData.name = room.name
  formData.capacity = room.capacity
  formData.restrictedToCourseId = room.restrictedToCourseId || ''
  
  showRoomModal.value = true
}

const updateRoom = async (data: any) => {
  if (!editingRoom.value) return
  await roomStore.updateRoom(editingRoom.value.id, data)
  closeModal()
  toast.success('Sala atualizada com sucesso')
  fetchRooms(currentPage.value)
}

const deleteRoom = async (id: number) => {
  if (confirm('Tem certeza que deseja excluir esta sala?')) {
    await roomStore.deleteRoom(id)
    toast.success('Sala removida com sucesso')
    fetchRooms(currentPage.value)
  }
}

const openRoomModal = async () => {
  editingRoom.value = null
  formData.name = ''
  formData.capacity = null
  formData.restrictedToCourseId = ''
  
  await courseStore.fetchAllCoursesSimple()
  showRoomModal.value = true
}

const closeModal = () => {
  showRoomModal.value = false
  editingRoom.value = null
}

onMounted(fetchRooms)
</script>
