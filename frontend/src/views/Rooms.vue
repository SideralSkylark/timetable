<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">Gerir Salas</h1>

    <!-- Botão para abrir o formulário de criação -->
    <button
      v-if="!showCreateForm"
      @click="showCreateForm = true"
      class="mb-4 px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
    >
      Criar nova sala
    </button>

    <!-- Formulário de criação -->
    <CrudForm
      v-if="showCreateForm"
      :fields="createFields"
      title="Criar nova sala"
      isCreate
      @submit="createRoom"
      @cancel="showCreateForm = false"
    />

    <!-- Formulário de edição -->
    <CrudForm
      v-if="editingRoom"
      :fields="editFields"
      :data="editingRoom"
      title="Editar sala"
      @submit="updateRoom"
      @cancel="editingRoom = null"
    />

    <!-- Tabela de salas -->
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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoomStore } from '@/stores/room'
import type { RoomResponse } from '@/services/dto/room'
import CrudForm from '@/component/ui/CrudForm.vue'
import CrudTable from '@/component/ui/CrudTable.vue'

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
