<template>
  <div>

    <!-- Header -->
    <PageHeader
      :icon="Building"
      title="Gestão de salas"
      subtitle="Gerir as salas da instituição"
    >
      <template #actions>
        <button @click="openRoomModal"
          class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition text-sm font-medium">
          <Plus class="w-4 h-4" />
          Nova sala
        </button>
      </template>
    </PageHeader>

    <!-- Filters -->
    <FilterBar :activeFilterCount="activeFilterCount" @clear="clearFilters">
      <template #filters>
        <!-- Search by name -->
        <div class="flex flex-col gap-1">
          <label class="text-[10px] font-bold text-blue-800 uppercase tracking-wider">Nome</label>
          <div class="relative">
            <input
              v-model="filters.name"
              type="text"
              placeholder="Pesquisar sala..."
              class="h-8 pl-8 pr-3 border border-gray-200 rounded-md text-sm text-gray-800 bg-white focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition placeholder:text-gray-300"
              style="width: 180px;"
            />
            <Search class="w-3.5 h-3.5 text-gray-300 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          </div>
        </div>

        <!-- Capacity range -->
        <div class="flex flex-col gap-1">
          <label class="text-[10px] font-bold text-blue-800 uppercase tracking-wider">Capacidade</label>
          <div class="flex items-center gap-1.5">
            <input
              v-model.number="filters.capacityMin"
              type="number"
              min="0"
              placeholder="Mín"
              class="h-8 w-20 px-3 border border-gray-200 rounded-md text-sm text-gray-800 focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition placeholder:text-gray-300"
            />
            <span class="text-xs text-gray-300">—</span>
            <input
              v-model.number="filters.capacityMax"
              type="number"
              min="0"
              placeholder="Máx"
              class="h-8 w-20 px-3 border border-gray-200 rounded-md text-sm text-gray-800 focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition placeholder:text-gray-300"
            />
          </div>
        </div>

        <!-- Assigned course -->
        <div class="flex flex-col gap-1">
          <label class="text-[10px] font-bold text-blue-800 uppercase tracking-wider">Curso</label>
          <div class="relative">
            <select
              v-model="filters.courseId"
              class="h-8 px-3 pr-8 border border-gray-200 rounded-md text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer"
              style="width: 180px;"
            >
              <option value="">Todos os cursos</option>
              <option v-for="course in availableCourses" :key="course.id" :value="course.id">
                {{ course.name }}
              </option>
            </select>
            <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          </div>
        </div>

        <!-- Restriction period -->
        <div class="flex flex-col gap-1">
          <label class="text-[10px] font-bold text-blue-800 uppercase tracking-wider">Período</label>
          <div class="relative">
            <select
              v-model="filters.period"
              class="h-8 px-3 pr-8 border border-gray-200 rounded-md text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer"
            >
              <option value="">Todos os períodos</option>
              <option value="MORNING">Manhã</option>
              <option value="AFTERNOON">Tarde</option>
              <option value="EVENING">Noite</option>
            </select>
            <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          </div>
        </div>
      </template>
    </FilterBar>

    <div>
      <!-- Delete confirmation banner -->
      <DeleteConfirmBanner
        v-if="confirmDeleteId !== null"
        :message="`Tem certeza que deseja deletar a sala '<strong class='text-red-700'>${roomToDelete?.name}</strong>'?`"
        @cancel="confirmDeleteId = null; roomToDelete = null"
        @confirm="confirmDelete(confirmDeleteId!)"
      />

      <!-- Table -->
      <CrudTable
        :columns="tableColumns"
        :rows="filteredRooms"
        :currentPage="currentPage"
        :totalPages="pagedRooms?.page.totalPages ?? 0"
        @edit="openEditRoomModal"
        @delete="(id: number) => { confirmDeleteId = id; roomToDelete = filteredRooms.find(r => r.id === id) || null }"
        @change-page="fetchRooms"
      >
        <template #empty>
          <DoorOpen class="w-8 h-8 mx-auto mb-3 text-gray-300" />
          <p>{{ activeFilterCount > 0 ? 'Nenhuma sala corresponde aos filtros' : 'Nenhuma sala registada' }}</p>
        </template>

        <template #cell-name="{ value }">
          <span class="font-medium text-gray-800">{{ value }}</span>
        </template>

        <template #cell-capacity="{ value }">
          <span :class="capacityBadgeClass(value)"
            class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium">
            {{ value }} lugares
          </span>
        </template>

        <template #cell-assignedCourses="{ value }">
          <span class="text-gray-500 text-sm">{{ value }}</span>
        </template>
      </CrudTable>
    </div>

    <!-- Modal -->
    <div v-if="showRoomModal" class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
      @click.self="closeModal">
      <div class="bg-white rounded-[10px] shadow-2xl max-w-md w-full max-h-[90vh] overflow-y-auto border border-gray-100">

        <div class="p-5 border-b border-gray-100 flex items-center gap-3">
          <div :class="editingRoom ? 'bg-amber-50' : 'bg-blue-50'" class="p-2 rounded-md">
            <DoorOpen v-if="!editingRoom" class="w-4 h-4 text-blue-900" />
            <Edit v-else class="w-4 h-4 text-amber-600" />
          </div>
          <div>
            <h2 class="text-base font-semibold text-gray-900">
              {{ editingRoom ? 'Editar sala' : 'Nova sala' }}
            </h2>
            <p v-if="editingRoom" class="text-xs text-gray-400 mt-0.5">{{ editingRoom.name }}</p>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" novalidate class="p-5 space-y-5">

          <div>
            <label class="flex items-center gap-1.5 text-[10px] font-bold text-blue-800 uppercase tracking-wider mb-1.5">
              <Tag class="w-3.5 h-3.5" />
              Nome da sala <span class="text-blue-900">*</span>
            </label>
            <input v-model="formData.name" type="text"
              class="w-full px-3 py-2 border rounded-md text-sm outline-none transition text-gray-800 placeholder:text-gray-300"
              :class="formErrors.name ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'"
              placeholder="Ex: Sala A101, Laboratório 3…" />
            <p v-if="formErrors.name" class="text-red-500 text-[10px] mt-1">O nome da sala é obrigatório</p>
          </div>

          <div>
            <label class="flex items-center gap-1.5 text-[10px] font-bold text-blue-800 uppercase tracking-wider mb-1.5">
              <UsersIcon class="w-3.5 h-3.5" />
              Capacidade <span class="text-blue-900">*</span>
            </label>
            <input v-model.number="formData.capacity" type="number" min="1"
              class="w-full px-3 py-2 border rounded-md text-sm outline-none transition text-gray-800 placeholder:text-gray-300"
              :class="formErrors.capacity ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'"
              placeholder="Número de lugares" />
            <p v-if="formErrors.capacity" class="text-red-500 text-[10px] mt-1">A capacidade deve ser pelo menos 1</p>
          </div>

          <div>
            <div class="flex items-center justify-between mb-2">
              <label class="flex items-center gap-1.5 text-[10px] font-bold text-blue-800 uppercase tracking-wider">
                <ShieldAlert class="w-3.5 h-3.5" />
                Restrições de acesso
              </label>
              <span v-if="formData.restrictions.length > 0" class="text-xs text-gray-400">
                {{ formData.restrictions.length }} definida(s)
              </span>
            </div>

            <div v-if="formData.restrictions.length === 0"
              class="border border-dashed border-gray-200 rounded-md py-5 text-center text-gray-400 text-xs mb-2">
              Sem restrições — esta sala é de acesso livre.
            </div>

            <div v-else class="space-y-2 mb-2">
              <div v-for="(restriction, index) in formData.restrictions" :key="index"
                class="border border-gray-200 rounded-md overflow-hidden">
                <div class="flex items-center justify-between px-3 py-1.5 bg-gray-50 border-b border-gray-100">
                  <span class="text-xs text-gray-400 font-medium">Restrição {{ index + 1 }}</span>
                  <button type="button" @click="formData.restrictions.splice(index, 1)"
                    class="text-xs text-red-400 hover:text-red-600 transition flex items-center gap-1">
                    <X class="w-3 h-3" /> Remover
                  </button>
                </div>
                <div class="grid grid-cols-2 divide-x divide-gray-100">
                  <div class="px-3 py-2.5">
                    <p class="text-xs text-gray-400 mb-1">Curso</p>
                    <select v-model="restriction.courseId"
                      class="w-full text-sm text-gray-700 bg-transparent border-none outline-none p-0">
                      <option :value="null" disabled>Selecionar…</option>
                      <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
                        {{ course.name }}
                      </option>
                    </select>
                  </div>
                  <div class="px-3 py-2.5">
                    <p class="text-xs text-gray-400 mb-1">Período</p>
                    <select v-model="restriction.period"
                      class="w-full text-sm text-gray-700 bg-transparent border-none outline-none p-0">
                      <option :value="null" disabled>Selecionar…</option>
                      <option value="MORNING">Manhã</option>
                      <option value="AFTERNOON">Tarde</option>
                      <option value="EVENING">Noite</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            <button type="button"
              @click="formData.restrictions.push({ courseId: null, period: null })"
              class="w-full border border-dashed border-blue-200 text-blue-900 hover:bg-blue-50 rounded-lg py-2 text-xs flex items-center justify-center gap-1.5 transition">
              <Plus class="w-3.5 h-3.5" />
              Adicionar restrição
            </button>
          </div>

          <div class="flex gap-2 pt-1">
            <button type="button" @click="closeModal"
              class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center justify-center gap-1.5">
              <X class="w-3.5 h-3.5" />
              Cancelar
            </button>
            <button type="submit"
              :disabled="isSubmitting"
              :class="{ 'opacity-50 cursor-not-allowed': isSubmitting }"
              class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg text-sm hover:bg-blue-800 transition flex items-center justify-center gap-1.5 font-medium">
              <template v-if="isSubmitting">
                <Loader2 class="w-3.5 h-3.5 animate-spin" />
                <span>{{ editingRoom ? 'Updating...' : 'Creating...' }}</span>
              </template>
              <template v-else>
                <Check class="w-3.5 h-3.5" />
                <span>{{ editingRoom ? 'Update Room' : 'Create Room' }}</span>
              </template>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRoomStore } from '@/stores/room'
import { useCourseStore } from '@/stores/course'
import { useToast } from '@/composables/useToast'
import type { RoomResponse } from '@/services/dto/room'
import CrudTable from '@/component/ui/CrudTable.vue'
import PageHeader from '@/component/ui/PageHeader.vue'
import FilterBar from '@/component/ui/FilterBar.vue'
import DeleteConfirmBanner from '@/component/ui/DeleteConfirmBanner.vue'
import {
  Building, Plus, DoorOpen, Edit, Tag,
  Users as UsersIcon, X, Check, ShieldAlert,
  Search, ChevronDown, Loader2
} from 'lucide-vue-next'

const roomStore = useRoomStore()
const courseStore = useCourseStore()
const toast = useToast()
const editingRoom = ref<RoomResponse | null>(null)
const showRoomModal = ref(false)
const confirmDeleteId = ref<number | null>(null)
const roomToDelete = ref<RoomResponse | null>(null)
const isSubmitting = ref(false)
const pagedRooms = computed(() => roomStore.pagedRooms)
const currentPage = ref(0)

// ── Filters ───────────────────────────────────────────────────────
const filters = reactive({
  name: '',
  capacityMin: null as number | null,
  capacityMax: null as number | null,
  courseId: '' as number | '',
  period: '',
})

const activeFilterCount = computed(() => [
  filters.name.trim() !== '',
  filters.capacityMin !== null,
  filters.capacityMax !== null,
  filters.courseId !== '',
  filters.period !== '',
].filter(Boolean).length)

const clearFilters = () => {
  filters.name = ''
  filters.capacityMin = null
  filters.capacityMax = null
  filters.courseId = ''
  filters.period = ''
}

// Use all courses from store for filtering
const availableCourses = computed(() => 
  courseStore.courses.sort((a, b) => a.name.localeCompare(b.name))
)

const mappedRooms = computed(() =>
  (pagedRooms.value?.content ?? []).map(room => ({
    ...room,
    assignedCourses: [
      ...new Map(room.restrictions.map(r => [r.courseId, r.courseName])).values()
    ].join(', ') || '—',
  }))
)

// Now filteredRooms is just mappedRooms since filtering is done on the API
const filteredRooms = computed(() => mappedRooms.value)

// ── Form ──────────────────────────────────────────────────────────
const formData = reactive({
  name: '',
  capacity: null as number | null,
  restrictions: [] as Array<{ courseId: number | null; period: string | null }>
})

const formErrors = reactive({
  name: false,
  capacity: false
})

function buildPeriodRestrictions() {
  const periodMap: Record<string, number[]> = {}
  for (const restriction of formData.restrictions) {
    if (!restriction.courseId || !restriction.period) continue
    let courseIds = periodMap[restriction.period]
    if (!courseIds) {
      courseIds = []
      periodMap[restriction.period] = courseIds
    }
    // prevent duplicate restriction entries
    if (!courseIds.includes(restriction.courseId)) {
      courseIds.push(restriction.courseId)
    }
  }
  return periodMap
}

const tableColumns = [
  { key: 'name', label: 'Nome' },
  { key: 'capacity', label: 'Capacidade' },
  { key: 'assignedCourses', label: 'Cursos atribuídos' },
]

const capacityBadgeClass = (capacity: number) => {
  if (capacity <= 30) return 'bg-blue-50 text-blue-700'
  if (capacity <= 60) return 'bg-blue-100 text-blue-800'
  return 'bg-blue-200 text-blue-900'
}

const fetchRooms = async (page = 0) => {
  currentPage.value = page
  const backendFilters = {
    name: filters.name.trim() || undefined,
    capacityMin: filters.capacityMin || undefined,
    capacityMax: filters.capacityMax || undefined,
    courseId: filters.courseId || undefined,
    period: filters.period || undefined,
  }
  await roomStore.fetchRooms(page, 10, backendFilters)
}

// Watch filters to trigger re-fetch
watch(filters, () => fetchRooms(0))

const handleSubmit = async () => {
  formErrors.name = !formData.name.trim()
  formErrors.capacity = formData.capacity === null || formData.capacity <= 0

  if (formErrors.name || formErrors.capacity) {
    toast.error('O nome da sala e a capacidade são campos obrigatórios.')
    return
  }

  isSubmitting.value = true
  try {
    const data: any = {
      name: formData.name,
      capacity: formData.capacity,
    }
    const periodRestrictions = buildPeriodRestrictions()
    if (Object.keys(periodRestrictions).length > 0) {
      data.periodRestrictions = periodRestrictions
    }
    editingRoom.value ? await updateRoom(data) : await createRoom(data)
  } finally {
    isSubmitting.value = false
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
  formData.name = room.name
  formData.capacity = room.capacity
  formData.restrictions = room.restrictions.map(r => ({ courseId: r.courseId, period: r.period }))
  formErrors.name = false
  formErrors.capacity = false
  showRoomModal.value = true
}

const updateRoom = async (data: any) => {
  if (!editingRoom.value) return
  await roomStore.updateRoom(editingRoom.value.id, data)
  closeModal()
  toast.success('Sala atualizada com sucesso')
  fetchRooms(currentPage.value)
}

const confirmDelete = async (id: number) => {
  await roomStore.deleteRoom(id)
  confirmDeleteId.value = null
  roomToDelete.value = null
  toast.success('Sala removida com sucesso')
  fetchRooms(currentPage.value)
}

const openRoomModal = async () => {
  editingRoom.value = null
  formData.name = ''
  formData.capacity = null
  formData.restrictions = []
  formErrors.name = false
  formErrors.capacity = false
  await courseStore.fetchAllCoursesSimple()
  showRoomModal.value = true
}

const closeModal = () => {
  showRoomModal.value = false
  editingRoom.value = null
}

onMounted(() => {
  fetchRooms()
  courseStore.fetchAllCoursesSimple()
})
</script>
