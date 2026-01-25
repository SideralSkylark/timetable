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
              <p class="text-gray-500 text-sm">Gerir as salas da instituicao</p>
            </div>
          </div>

          <button
            @click="openRoomModal()"
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
      <!-- Criar Sala -->
      <CrudForm
        v-if="!editingRoom"
        :fields="roomFields"
        title="Nova Sala"
        is-create
        @cancel="() => { showRoomModal = false }"
        @submit="createRoom"
      />

      <!-- Editar Sala -->
      <CrudForm
        v-else
        :fields="roomFields"
        :title="`Editar Sala — ${editingRoom.name}`"
        :data="editingRoom"
        @cancel="() => { showRoomModal = false; editingRoom = null }"
        @submit="updateRoom"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoomStore } from '@/stores/room'
import { useToast } from '@/composables/useToast'
import type { RoomResponse } from '@/services/dto/room'
import CrudForm from '@/component/ui/CrudForm.vue'
import CrudTable from '@/component/ui/CrudTable.vue'

import { Building, Plus } from 'lucide-vue-next'
import { useCourseStore } from '@/stores/course'

const roomStore = useRoomStore()
const courseStore = useCourseStore()
const toast = useToast()
const editingRoom = ref<RoomResponse | null>(null)
const showRoomModal = ref(false)
const pagedRooms = computed(() => roomStore.pagedRooms)
const currentPage = ref(0)

const tableColumns = [
  { key: 'name', label: 'Nome' },
  { key: 'capacity', label: 'Capacidade' },
  { key: 'restrictedToCourseName', label: 'Atribuido'},
]

const roomFields = computed(() => [
  { name: 'name', type: 'text', placeholder: 'Nome da sala', required: true },
  { name: 'capacity', type: 'number', placeholder: 'Capacidade', required: true },
  {
    name: 'restrictedToCourseId',
    type: 'select',
    label: 'Atribuído ao curso',
    options: courseStore.courses.map(c => ({
      label: c.name,
      value: c.id,
    })),
    placeholder: 'Selecione um curso',
    required: true,  
  },
])

const fetchRooms = async (page = 0) => {
  currentPage.value = page
  await roomStore.fetchRooms(page, 10)
}

const createRoom = async (data: any) => {
  await roomStore.createRoom(data)
  showRoomModal.value = false
  toast.success('Sala criada com sucesso!')
  fetchRooms(currentPage.value)
}

const openEditRoomModal = async (room: RoomResponse) => {
  await courseStore.fetchAllCoursesSimple()
  editingRoom.value = { ...room }
  showRoomModal.value = true
}

const updateRoom = async (data: any) => {
  if (!editingRoom.value) return
  await roomStore.updateRoom(editingRoom.value.id, data)
  editingRoom.value = null
  toast.success('Sala atualizada com sucesso')
  fetchRooms(currentPage.value)
  showRoomModal.value = false
}

const deleteRoom = async (id: number) => {
  await roomStore.deleteRoom(id)
  toast.success('Sala removida com sucesso')
  fetchRooms(currentPage.value)
}

const openRoomModal = async () => {
  editingRoom.value = null
  await courseStore.fetchAllCoursesSimple()
  showRoomModal.value = true
}

onMounted(fetchRooms)
</script>
