<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- Header -->
    <div class="max-w-6xl mx-auto mb-8">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="bg-blue-900 p-3 rounded-lg">
              <GraduationCap class="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">Gestão de Cursos</h1>
              <p class="text-gray-500 text-sm">Gerir cursos e suas disciplinas</p>
            </div>
          </div>

          <button
            @click="openCourseModal"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition"
          >
            <Plus class="w-5 h-5" />
            Novo Curso
          </button>
        </div>
      </div>
    </div>

    <!-- Lista de Cursos -->
    <div class="max-w-6xl mx-auto space-y-4">
      <div
        v-for="course in courses"
        :key="course.id"
        class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden"
      >
        <!-- Course Header -->
        <div class="p-5 flex items-center justify-between hover:bg-gray-50 transition">
          <div class="flex items-center gap-4 flex-1">
            <button
              @click="toggleCourse(course.id)"
              class="text-gray-600 hover:text-blue-900 transition"
            >
              <ChevronDown v-if="course.expanded" class="w-5 h-5" />
              <ChevronRight v-else class="w-5 h-5" />
            </button>

            <div class="bg-blue-900 bg-opacity-10 p-2 rounded">
              <BookOpen class="w-5 h-5 text-blue-900" />
            </div>

            <div class="flex-1">
              <h3 class="font-semibold text-gray-900">{{ course.name }}</h3>
              <div class="flex items-center gap-4 mt-1">
                <span class="text-sm text-gray-500">Duração: {{ course.duration }}</span>
                <span class="text-sm text-gray-500">•</span>
                <span class="text-sm text-gray-500">{{ course.disciplines.length }} disciplinas</span>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <button
              class="p-2 text-gray-400 hover:text-blue-900 hover:bg-blue-50 rounded transition"
            >
              <Edit2 class="w-4 h-4" />
            </button>
            <button
              @click="deleteCourse(course.id)"
              class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition"
            >
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- Disciplinas -->
        <div v-if="course.expanded" class="border-t border-gray-200 bg-gray-50">
          <div class="p-5">
            <div class="flex items-center justify-between mb-4">
              <h4 class="font-medium text-gray-700">Disciplinas</h4>

              <button
                @click="openDisciplineModal(course)"
                class="text-blue-900 text-sm flex items-center gap-1 hover:underline"
              >
                <Plus class="w-4 h-4" /> Adicionar Disciplina
              </button>
            </div>

            <div class="space-y-2">
              <div
                v-for="discipline in course.disciplines"
                :key="discipline.id"
                class="bg-white p-4 rounded-lg border border-gray-200 flex items-center justify-between"
              >
                <div>
                  <p class="font-medium text-gray-900">{{ discipline.name }}</p>
                  <p class="text-sm text-gray-500">{{ discipline.credits }} créditos</p>
                </div>

                <div class="flex items-center gap-2">
                  <button
                    class="p-2 text-gray-400 hover:text-blue-900 hover:bg-blue-50 rounded transition"
                  >
                    <Edit2 class="w-4 h-4" />
                  </button>

                  <button
                    @click="deleteDiscipline(course.id, discipline.id)"
                    class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Curso -->
    <div
      v-if="showCourseModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
    >
      <CrudForm
        :fields="courseFields"
        title="Novo Curso"
        is-create
        @cancel="showCourseModal = false"
        @submit="createCourse"
      />
    </div>

    <!-- Modal Disciplina -->
    <div
      v-if="showDisciplineModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
    >
      <CrudForm
        :fields="disciplineFields"
        :title="`Nova Disciplina — Curso: ${selectedCourse?.name}`"
        is-create
        @cancel="showDisciplineModal = false"
        @submit="createDiscipline"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CrudForm from '@/component/ui/CrudForm.vue'

import {
  Plus,
  Edit2,
  Trash2,
  ChevronDown,
  ChevronRight,
  BookOpen,
  GraduationCap,
} from 'lucide-vue-next'

// ======================
// State
// ======================
const courses = ref([
  {
    id: 1,
    name: 'Engenharia Informática',
    duration: '4 anos',
    disciplines: [
      { id: 1, name: 'Programação I', credits: 6 },
      { id: 2, name: 'Matemática Discreta', credits: 5 },
      { id: 3, name: 'Algoritmos', credits: 6 },
    ],
    expanded: false,
  },
  {
    id: 2,
    name: 'Gestão de Empresas',
    duration: '3 anos',
    disciplines: [
      { id: 4, name: 'Contabilidade', credits: 5 },
      { id: 5, name: 'Marketing', credits: 4 },
    ],
    expanded: false,
  },
])

const showCourseModal = ref(false)
const showDisciplineModal = ref(false)
const selectedCourse = ref<any>(null)

// ======================
// Fields for CrudForm
// ======================
const courseFields = [
  { name: 'name', type: 'text', placeholder: 'Nome do Curso' },
  { name: 'duration', type: 'text', placeholder: 'Ex: 4 anos' },
]

const disciplineFields = [
  { name: 'name', type: 'text', placeholder: 'Nome da Disciplina' },
  { name: 'credits', type: 'number', placeholder: 'Créditos' },
]

// ======================
// Handlers
// ======================
const toggleCourse = (id: number) => {
  courses.value = courses.value.map((c) =>
    c.id === id ? { ...c, expanded: !c.expanded } : c
  )
}

const deleteCourse = (id: number) => {
  courses.value = courses.value.filter((c) => c.id !== id)
}

const deleteDiscipline = (courseId: number, disciplineId: number) => {
  courses.value = courses.value.map((c) =>
    c.id === courseId
      ? {
          ...c,
          disciplines: c.disciplines.filter((d) => d.id !== disciplineId),
        }
      : c
  )
}

const openCourseModal = () => {
  showCourseModal.value = true
}

const openDisciplineModal = (course: any) => {
  selectedCourse.value = course
  showDisciplineModal.value = true
}

const createCourse = (data: any) => {
  courses.value.push({
    id: Date.now(),
    name: data.name,
    duration: data.duration,
    disciplines: [],
    expanded: false,
  })
  showCourseModal.value = false
}

const createDiscipline = (data: any) => {
  const target = selectedCourse.value
  target.disciplines.push({
    id: Date.now(),
    name: data.name,
    credits: Number(data.credits),
  })
  showDisciplineModal.value = false
}
</script>
