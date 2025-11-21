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
              <p class="text-gray-500 text-sm">Gerir salas e suas capacidades</p>
            </div>
          </div>

          <button
            v-if="!showCreateForm"
            @click="showCreateForm = true"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition"
          >
            <Plus class="w-5 h-5" />
            Nova Sala
          </button>
        </div>
      </div>
    </div>

    <!-- Formulário de criação -->
    <div v-if="showCreateForm" class="max-w-4xl mx-auto mb-8">
      <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <CrudForm
          :fields="createFields"
          title="Criar nova sala"
          is-create
          @submit="createRoom"
          @cancel="showCreateForm = false"
        />
      </div>
    </div>

    <!-- Formulário de edição -->
    <div v-if="editingRoom" class="max-w-4xl mx-auto mb-8">
      <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <CrudForm
          :fields="editFields"
          :data="editingRoom"
          title="Editar sala"
          @submit="updateRoom"
          @cancel="editingRoom = null"
        />
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
          @edit="openEdit"
          @delete="deleteRoom"
          @change-page="fetchRooms"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoomStore } from '@/stores/room'
import type { RoomResponse } from '@/services/dto/room'
import CrudForm from '@/component/ui/CrudForm.vue'
import CrudTable from '@/component/ui/CrudTable.vue'

import { Building, Plus } from 'lucide-vue-next'

const roomStore = useRoomStore()
const editingRoom = ref<RoomResponse | null>(null)
const showCreateForm = ref(false)
const pagedRooms = ref(roomStore.pagedRooms)
const currentPage = ref(0)

const tableColumns = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Nome' },
  { key: 'capacity', label: 'Capacidade' },
]

const createFields = [
  { name: 'name', type: 'text', placeholder: 'Nome da sala' },
  { name: 'capacity', type: 'number', placeholder: 'Capacidade' },
]

const editFields = [
  { name: 'name', type: 'text', placeholder: 'Nome da sala' },
  { name: 'capacity', type: 'number', placeholder: 'Capacidade' },
]

const fetchRooms = async (page = 0) => {
  currentPage.value = page
  await roomStore.fetchRooms(page, 10)
  pagedRooms.value = roomStore.pagedRooms
}

const createRoom = async (data: any) => {
  await roomStore.createRoom(data)
  showCreateForm.value = false
  fetchRooms(currentPage.value)
}

const openEdit = (room: RoomResponse) => {
  editingRoom.value = { ...room }
}

const updateRoom = async (data: any) => {
  if (!editingRoom.value) return
  await roomStore.updateRoom(editingRoom.value.id, data)
  editingRoom.value = null
  fetchRooms(currentPage.value)
}

const deleteRoom = async (id: number) => {
  await roomStore.deleteRoom(id)
  fetchRooms(currentPage.value)
}

onMounted(fetchRooms)
</script>
