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
              <h1 class="text-xl font-semibold text-gray-900">Turmas</h1>
              <p class="text-gray-400 text-sm">Confirmar ingressos e gerir turmas</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <div v-if="confirmationProgress"
              class="flex items-center gap-2 px-3 py-1.5 bg-amber-50 border border-amber-100 rounded-lg text-xs text-amber-700 font-medium">
              <AlertCircle class="w-3.5 h-3.5" />
              {{ confirmationProgress.confirmed }}/{{ confirmationProgress.total }} confirmadas
            </div>
            <button @click="openCreateModal"
              class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition text-sm font-medium">
              <Plus class="w-4 h-4" />
              Nova turma
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="mb-5">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 px-5 py-4">
        <div class="flex flex-wrap items-end gap-4">

          <!-- Turma name search -->
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Turma</label>
            <div class="relative">
              <input
                v-model="filters.name"
                type="text"
                placeholder="Ex: 1º Ano · Turma A..."
                class="h-8 pl-8 pr-3 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition placeholder:text-gray-300"
                style="width: 200px;"
              />
              <Search class="w-3.5 h-3.5 text-gray-300 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Course -->
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Curso</label>
            <div class="relative">
              <select
                v-model="filters.courseId"
                class="h-8 px-3 pr-8 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer"
                style="width: 180px;"
              >
                <option value="">Todos</option>
                <option v-for="c in availableCourses" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
              <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Academic year -->
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Ano lectivo</label>
            <div class="relative">
              <select
                v-model="filters.academicYear"
                class="h-8 px-3 pr-8 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer"
              >
                <option value="">Todos</option>
                <option v-for="y in availableAcademicYears" :key="y" :value="y">{{ y }}</option>
              </select>
              <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Semester -->
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Semestre</label>
            <div class="flex items-center gap-1 h-8">
              <button
                v-for="opt in semesterOptions" :key="opt.value"
                type="button"
                @click="filters.semester = opt.value"
                :class="filters.semester === opt.value
                  ? 'bg-blue-900 text-white border-blue-900'
                  : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'"
                class="h-8 px-3 text-xs font-medium border rounded-lg transition"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>

          <!-- Status -->
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-400 uppercase tracking-wider">Estado</label>
            <div class="relative">
              <select
                v-model="filters.status"
                class="h-8 px-3 pr-8 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer"
              >
                <option value="">Todos</option>
                <option value="ESTIMATED">Estimado</option>
                <option value="CONFIRMED">Confirmado</option>
                <option value="ACTIVE">Activo</option>
                <option value="ARCHIVED">Arquivado</option>
              </select>
              <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Clear -->
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

    <!-- Table -->
    <div>
      <!-- Delete confirmation banner -->
      <div v-if="confirmDeleteId !== null"
        class="mb-3 flex items-center justify-between bg-red-50 border border-red-100 rounded-lg px-4 py-3">
        <span class="text-sm text-red-700">Tem a certeza que quer eliminar esta turma?</span>
        <div class="flex gap-2">
          <button @click="confirmDeleteId = null"
            class="px-3 py-1.5 text-xs border border-gray-200 text-gray-500 rounded-md hover:bg-white transition">
            Cancelar
          </button>
          <button @click="handleDelete(confirmDeleteId!)"
            class="px-3 py-1.5 text-xs bg-red-600 text-white rounded-md hover:bg-red-700 transition font-medium">
            Eliminar
          </button>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-100">
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Turma</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Curso</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Ano lectivo</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Semestre</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Alunos</th>
              <th class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide">Estado</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredCohorts.length === 0">
              <td colspan="7" class="text-center py-16 text-gray-400 text-sm">
                <UsersIcon class="w-8 h-8 mx-auto mb-3 text-gray-300" />
                {{ activeFilterCount > 0 ? 'Nenhuma turma corresponde aos filtros' : 'Nenhuma turma encontrada' }}
              </td>
            </tr>
            <tr
              v-for="cohort in filteredCohorts"
              :key="cohort.id"
              class="border-b border-gray-50 last:border-0 hover:bg-gray-50 transition-colors group"
              :class="cohort.status === 'ESTIMATED' ? 'bg-amber-50/20' : ''"
            >
              <td class="px-5 py-3.5 font-medium text-gray-800">{{ cohort.turma }}</td>
              <td class="px-5 py-3.5 text-gray-500">{{ cohort.courseName }}</td>
              <td class="px-5 py-3.5 text-gray-500">{{ cohort.academicYear }}</td>
              <td class="px-5 py-3.5 text-gray-500">{{ cohort.semester }}º sem.</td>
              <td class="px-5 py-3.5">
                <span :class="cohort.status === 'ESTIMATED' ? 'text-amber-600 italic' : 'text-gray-700'">
                  {{ cohort.studentCount }}
                  <span v-if="cohort.status === 'ESTIMATED'" class="text-xs not-italic text-amber-500">(est.)</span>
                </span>
              </td>
              <td class="px-5 py-3.5">
                <span :class="statusBadgeClass(cohort.status)"
                  class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium">
                  <Clock v-if="cohort.status === 'ESTIMATED'" class="w-3 h-3" />
                  <CheckCircle v-else-if="cohort.status === 'CONFIRMED'" class="w-3 h-3" />
                  {{ statusLabels[cohort.status] ?? cohort.status }}
                </span>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center justify-end gap-2">
                  <button v-if="cohort.status === 'ESTIMATED'"
                    @click="openConfirmModal(cohort)"
                    class="flex items-center gap-1 px-2.5 py-1 bg-green-600 text-white text-xs rounded-md hover:bg-green-700 transition opacity-0 group-hover:opacity-100">
                    <CheckCircle class="w-3 h-3" />
                    Confirmar
                  </button>
                  <button v-else
                    @click="openEditModal(cohort)"
                    class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-amber-600 hover:border-amber-200 hover:bg-amber-50 transition opacity-0 group-hover:opacity-100">
                    <Edit class="w-3.5 h-3.5" />
                  </button>
<button
  @click="openStudentsModal(cohort)"
  class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-blue-900 hover:border-blue-200 hover:bg-blue-50 transition opacity-0 group-hover:opacity-100"
  title="Gerir estudantes">
  <UsersIcon class="w-3.5 h-3.5" />
</button>
                  <button
                    @click="confirmDeleteId = cohort.id"
                    class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-red-600 hover:border-red-200 hover:bg-red-50 transition opacity-0 group-hover:opacity-100">
                    <Trash2 class="w-3.5 h-3.5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div class="border-t border-gray-100">
          <Pagination
            :page="currentPage"
            :totalPages="cohortStore.cohortsPage?.page.totalPages ?? 1"
            @update:page="fetchCohorts"
          />
        </div>
      </div>
    </div>

    <!-- Modal: Confirm enrolments -->
    <div v-if="showConfirmModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
      @click.self="showConfirmModal = false">
      <div class="bg-white rounded-xl shadow-2xl max-w-md w-full border border-gray-100">

        <div class="p-5 border-b border-gray-100 flex items-center gap-3">
          <div class="bg-green-50 p-2 rounded-lg">
            <CheckCircle class="w-4 h-4 text-green-600" />
          </div>
          <div>
            <h2 class="text-base font-semibold text-gray-900">Confirmar ingressos</h2>
            <p class="text-xs text-gray-400 mt-0.5">
              {{ confirmingCohort?.turma }} · {{ confirmingCohort?.courseName }}
            </p>
          </div>
        </div>

        <div class="p-5 space-y-4">
          <div class="flex items-start gap-2.5 bg-amber-50 border border-amber-100 rounded-lg px-3 py-2.5">
            <AlertCircle class="w-4 h-4 text-amber-500 mt-0.5 shrink-0" />
            <p class="text-xs text-amber-700">
              Estimativa inicial: <strong>{{ confirmingCohort?.studentCount }} alunos</strong>.
              Insira o número real de ingressos confirmados.
            </p>
          </div>

          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <UsersIcon class="w-3.5 h-3.5" />
              Número de ingressos <span class="text-blue-900">*</span>
            </label>
            <input
              v-model.number="confirmForm.studentCount"
              type="number"
              min="1"
              :max="cohortStore.maxRoomCapacity ?? 200"
              class="w-full px-3 py-2 border rounded-lg text-sm outline-none transition text-gray-800"
              :class="confirmFormErrors.studentCount ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'"
            />
            <p v-if="confirmFormErrors.studentCount" class="text-red-500 text-[10px] mt-1">O número de ingressos é obrigatório</p>
            <p v-if="cohortStore.maxRoomCapacity && confirmForm.studentCount > cohortStore.maxRoomCapacity"
              class="text-xs text-red-500 mt-1.5 flex items-center gap-1">
              <AlertCircle class="w-3 h-3" />
              Excede a capacidade máxima das salas ({{ cohortStore.maxRoomCapacity }} alunos).
              Considere criar uma turma adicional.
            </p>
            <p v-else class="text-xs text-gray-400 mt-1.5">
              Capacidade máxima das salas: {{ cohortStore.maxRoomCapacity }} alunos
            </p>
          </div>

          <div class="flex gap-2 pt-1">
            <button type="button" @click="showConfirmModal = false"
              class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center justify-center gap-1.5">
              <X class="w-3.5 h-3.5" />
              Cancelar
            </button>
            <button @click="handleConfirm"
              class="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg text-sm hover:bg-green-700 transition flex items-center justify-center gap-1.5 font-medium">
              <CheckCircle class="w-3.5 h-3.5" />
              Confirmar ingressos
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Create / Edit -->
    <div v-if="showModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
      @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-2xl max-w-md w-full max-h-[90vh] overflow-y-auto border border-gray-100">

        <div class="p-5 border-b border-gray-100 flex items-center gap-3">
          <div :class="isEditing ? 'bg-amber-50' : 'bg-blue-50'" class="p-2 rounded-lg">
            <Edit v-if="isEditing" class="w-4 h-4 text-amber-600" />
            <UserPlus v-else class="w-4 h-4 text-blue-900" />
          </div>
          <h2 class="text-base font-semibold text-gray-900">
            {{ isEditing ? 'Editar turma' : 'Nova turma' }}
          </h2>
        </div>

        <div v-if="loadingDetail" class="flex items-center justify-center py-12">
          <Loader2 class="w-5 h-5 animate-spin text-blue-900" />
        </div>

        <form v-else @submit.prevent="handleSubmit" novalidate class="p-5 space-y-4">

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Ano curricular <span class="text-blue-900">*</span>
              </label>
              <input v-model.number="form.year" type="number" min="1" max="6"
                class="w-full px-3 py-2 border rounded-lg text-sm outline-none transition text-gray-800"
                :class="formErrors.year ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'" />
              <p v-if="formErrors.year" class="text-red-500 text-[10px] mt-1">O ano curricular é obrigatório (1-6)</p>
            </div>
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Secção <span class="text-blue-900">*</span>
              </label>
              <input v-model="form.section" type="text" maxlength="2"
                class="w-full px-3 py-2 border rounded-lg text-sm outline-none transition text-gray-800 uppercase"
                :class="formErrors.section ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'" />
              <p v-if="formErrors.section" class="text-red-500 text-[10px] mt-1">A secção é obrigatória</p>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Ano lectivo <span class="text-blue-900">*</span>
              </label>
              <input v-model.number="form.academicYear" type="number"
                class="w-full px-3 py-2 border rounded-lg text-sm outline-none transition text-gray-800"
                :class="formErrors.academicYear ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'" />
              <p v-if="formErrors.academicYear" class="text-red-500 text-[10px] mt-1">O ano lectivo é obrigatório</p>
            </div>
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Semestre <span class="text-blue-900">*</span>
              </label>
              <select v-model.number="form.semester"
                class="w-full px-3 py-2 border rounded-lg text-sm outline-none transition text-gray-800"
                :class="formErrors.semester ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'">
                <option value="" disabled>Selecionar</option>
                <option :value="1">1º semestre</option>
                <option :value="2">2º semestre</option>
              </select>
              <p v-if="formErrors.semester" class="text-red-500 text-[10px] mt-1">O semestre é obrigatório</p>
            </div>
          </div>

          <div v-if="!isEditing">
            <label class="text-xs font-medium text-gray-500 mb-1.5 block">
              Curso <span class="text-blue-900">*</span>
            </label>
            <select v-model.number="form.courseId"
              class="w-full px-3 py-2 border rounded-lg text-sm outline-none transition text-gray-800"
              :class="formErrors.courseId ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'border-gray-200 focus:ring-blue-100 focus:border-blue-900 focus:ring-2'">
              <option value="" disabled>Selecionar curso</option>
              <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
                {{ course.name }}
              </option>
            </select>
            <p v-if="formErrors.courseId" class="text-red-500 text-[10px] mt-1">O curso é obrigatório</p>
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
              {{ isEditing ? 'Guardar alterações' : 'Criar turma' }}
            </button>
          </div>
        </form>
      </div>
    </div>
<!-- Modal: Gerir estudantes -->
<div v-if="showStudentsModal"
  class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
  @click.self="showStudentsModal = false">
  <div class="bg-white rounded-xl shadow-2xl w-full max-w-lg max-h-[85vh] flex flex-col border border-gray-100">

    <div class="p-5 border-b border-gray-100 flex items-center gap-3 shrink-0">
      <div class="bg-blue-50 p-2 rounded-lg">
        <UsersIcon class="w-4 h-4 text-blue-900" />
      </div>
      <div>
        <h2 class="text-base font-semibold text-gray-900">Gerir estudantes</h2>
        <p class="text-xs text-gray-400 mt-0.5">{{ studentsModalCohort?.turma }}</p>
      </div>
    </div>

    <!-- Search -->
    <div class="px-5 pt-4 shrink-0">
      <div class="relative">
        <input
          v-model="studentSearch"
          type="text"
          placeholder="Pesquisar estudante..."
          class="w-full h-8 pl-8 pr-3 border border-gray-200 rounded-lg text-sm text-gray-800 bg-white focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition placeholder:text-gray-300"
        />
        <Search class="w-3.5 h-3.5 text-gray-300 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
      </div>
      <p class="text-xs text-gray-400 mt-2">
        {{ selectedStudentIds.length }} estudante(s) seleccionado(s)
      </p>
    </div>

    <!-- Student list -->
    <div class="flex-1 overflow-y-auto px-5 py-3 space-y-1 min-h-0">
      <div v-if="loadingStudents" class="flex justify-center py-8">
        <Loader2 class="w-5 h-5 animate-spin text-blue-900" />
      </div>
      <template v-else>
        <label
          v-for="student in filteredStudentList"
          :key="student.id"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg cursor-pointer hover:bg-gray-50 transition"
          :class="selectedStudentIds.includes(student.id) ? 'bg-blue-50' : ''">
          <input
            type="checkbox"
            :value="student.id"
            v-model="selectedStudentIds"
            class="w-4 h-4 text-blue-900 rounded border-gray-300 focus:ring-blue-900"
          />
          <div>
            <span class="text-sm font-medium text-gray-800">{{ student.username }}</span>
            <span class="text-xs text-gray-400 ml-2">{{ student.email }}</span>
          </div>
        </label>
        <div v-if="filteredStudentList.length === 0" class="text-center py-8 text-gray-400 text-sm">
          Nenhum estudante encontrado
        </div>
      </template>
    </div>

    <div class="p-5 border-t border-gray-100 flex gap-2 shrink-0">
      <button type="button" @click="showStudentsModal = false"
        class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center justify-center gap-1.5">
        <X class="w-3.5 h-3.5" />
        Cancelar
      </button>
      <button @click="handleSaveStudents"
        class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg text-sm hover:bg-blue-800 transition flex items-center justify-center gap-1.5 font-medium">
        <Check class="w-3.5 h-3.5" />
        Guardar
      </button>
    </div>
  </div>
</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useCohortStore } from '@/stores/cohorts'
import { useUserStore } from '@/stores/user'
import { useCourseStore } from '@/stores/course'
import { useToast } from '@/composables/useToast'
import type { CohortListResponse } from '@/services/dto/cohort'
import Pagination from '@/component/ui/Pagination.vue'
import {
  Users as UsersIcon,
  Plus,
  Edit,
  UserPlus,
  CheckCircle,
  Clock,
  AlertCircle,
  Trash2,
  Loader2,
  X,
  Check,
  Search,
  ChevronDown,
} from 'lucide-vue-next'

const cohortStore = useCohortStore()
const courseStore = useCourseStore()
const userStore = useUserStore()
const toast = useToast()

const currentPage = ref(0)
const showModal = ref(false)
const showConfirmModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const loadingDetail = ref(false)
const confirmDeleteId = ref<number | null>(null)
const confirmingCohort = ref<(CohortListResponse & { turma: string }) | null>(null)
const confirmForm = reactive({ studentCount: 35 })
const confirmFormErrors = reactive({ studentCount: false })

const form = reactive({
  year: 1,
  section: '',
  academicYear: new Date().getFullYear(),
  semester: 1 as number | '',
  courseId: '' as number | '',
  studentIds: [] as number[],
})

const formErrors = reactive({
  year: false,
  section: false,
  academicYear: false,
  semester: false,
  courseId: false,
})

// ── Students modal ────────────────────────────────────────────────
const showStudentsModal = ref(false)
const studentsModalCohort = ref<(CohortListResponse & { turma: string }) | null>(null)
const selectedStudentIds = ref<number[]>([])
const studentSearch = ref('')
const loadingStudents = ref(false)
const allStudents = ref<{ id: number; username: string; email: string }[]>([])

const filteredStudentList = computed(() => {
  const q = studentSearch.value.trim().toLowerCase()
  if (!q) return allStudents.value
  return allStudents.value.filter(s =>
    s.username.toLowerCase().includes(q) ||
    s.email.toLowerCase().includes(q)
  )
})

async function openStudentsModal(cohort: CohortListResponse & { turma: string }) {
  studentsModalCohort.value = cohort
  studentSearch.value = ''
  loadingStudents.value = true
  showStudentsModal.value = true

  try {
    const [students, detail] = await Promise.all([
      userStore.fetchStudents(),
      cohortStore.fetchCohort(cohort.id),
    ])
    allStudents.value = students
    selectedStudentIds.value = cohortStore.selectedCohort?.studentIds ?? []
  } finally {
    loadingStudents.value = false
  }
}

async function handleSaveStudents() {
  if (!studentsModalCohort.value) return
  try {
    await cohortStore.updateCohortStudents(
      studentsModalCohort.value.id,
      selectedStudentIds.value
    )
    toast.success('Estudantes actualizados com sucesso!')
    showStudentsModal.value = false
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao actualizar estudantes.')
  }
}

// ── Filters ───────────────────────────────────────────────────────
const filters = reactive({
  name: '',
  courseId: '' as number | '',
  academicYear: '' as number | '',
  semester: null as number | null,
  status: '',
})

const semesterOptions = [
  { label: 'Todos', value: null },
  { label: '1º sem.', value: 1 },
  { label: '2º sem.', value: 2 },
]

const activeFilterCount = computed(() => [
  filters.name.trim() !== '',
  filters.courseId !== '',
  filters.academicYear !== '',
  filters.semester !== null,
  filters.status !== '',
].filter(Boolean).length)

const clearFilters = () => {
  filters.name = ''
  filters.courseId = ''
  filters.academicYear = ''
  filters.semester = null
  filters.status = ''
}

// Unique courses from loaded cohorts for the dropdown
const availableCourses = computed(() => {
  const map = new Map<number, string>()
  for (const c of cohortStore.cohortsPage?.content ?? []) {
    if (c.courseId && c.courseName) map.set(c.courseId, c.courseName)
  }
  return [...map.entries()]
    .map(([id, name]) => ({ id, name }))
    .sort((a, b) => a.name.localeCompare(b.name))
})

// Unique academic years from loaded cohorts
const availableAcademicYears = computed(() => {
  const years = new Set<number>()
  for (const c of cohortStore.cohortsPage?.content ?? []) {
    if (c.academicYear) years.add(c.academicYear)
  }
  return [...years].sort((a, b) => b - a)
})

const mappedCohorts = computed(() =>
  (cohortStore.cohortsPage?.content ?? []).map((c: CohortListResponse) => ({
    ...c,
    turma: `${c.year}º Ano · Turma ${c.section}`,
  }))
)

const filteredCohorts = computed(() => {
  return mappedCohorts.value.filter(cohort => {
    if (filters.name.trim() && !cohort.turma.toLowerCase().includes(filters.name.trim().toLowerCase())) return false
    if (filters.courseId !== '' && cohort.courseId !== filters.courseId) return false
    if (filters.academicYear !== '' && cohort.academicYear !== filters.academicYear) return false
    if (filters.semester !== null && cohort.semester !== filters.semester) return false
    if (filters.status !== '' && cohort.status !== filters.status) return false
    return true
  })
})

// ── Derived ───────────────────────────────────────────────────────
const confirmationProgress = computed(() => {
  const all = cohortStore.cohortsPage?.content ?? []
  if (all.length === 0) return null
  const confirmed = all.filter(c => c.status !== 'ESTIMATED').length
  return { confirmed, total: all.length }
})

const statusLabels: Record<string, string> = {
  ESTIMATED: 'Estimado',
  CONFIRMED: 'Confirmado',
  ACTIVE:    'Activo',
  ARCHIVED:  'Arquivado',
}

const statusBadgeClass = (status: string) => ({
  ESTIMATED: 'bg-amber-100 text-amber-700',
  CONFIRMED: 'bg-green-100 text-green-700',
  ACTIVE:    'bg-blue-100 text-blue-700',
  ARCHIVED:  'bg-gray-100 text-gray-500',
}[status] ?? 'bg-gray-100 text-gray-500')

// ── Lifecycle ─────────────────────────────────────────────────────
onMounted(() => fetchCohorts())

async function fetchCohorts(page = 0) {
  currentPage.value = page
  await cohortStore.fetchCohorts(page, 10)
}

// ── Confirm enrolments ────────────────────────────────────────────
async function openConfirmModal(cohort: CohortListResponse & { turma: string }) {
  confirmingCohort.value = cohort
  confirmForm.studentCount = cohort.studentCount
  confirmFormErrors.studentCount = false
  await cohortStore.fetchMaxRoomCapacity()
  showConfirmModal.value = true
}

async function handleConfirm() {
  if (!confirmingCohort.value) return
  confirmFormErrors.studentCount = !confirmForm.studentCount || confirmForm.studentCount < 1
  if (confirmFormErrors.studentCount) {
    toast.error('O número de ingressos deve ser pelo menos 1.')
    return
  }

  try {
    await cohortStore.confirmCohort(confirmingCohort.value.id, confirmForm.studentCount)
    toast.success('Ingressos confirmados!')
    showConfirmModal.value = false
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao confirmar ingressos.')
  }
}

// ── Create / Edit modal ───────────────────────────────────────────
async function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  form.year = 1
  form.section = ''
  form.academicYear = new Date().getFullYear()
  form.semester = 1
  form.courseId = ''
  form.studentIds = []

  formErrors.year = false
  formErrors.section = false
  formErrors.academicYear = false
  formErrors.semester = false
  formErrors.courseId = false

  await courseStore.fetchAllCoursesSimple()
  showModal.value = true
}

async function openEditModal(row: CohortListResponse) {
  isEditing.value = true
  editingId.value = row.id
  showModal.value = true
  loadingDetail.value = true

  formErrors.year = false
  formErrors.section = false
  formErrors.academicYear = false
  formErrors.semester = false
  formErrors.courseId = false

  await cohortStore.fetchCohort(row.id)
  const detail = cohortStore.selectedCohort
  if (detail) {
    form.year = detail.year
    form.section = detail.section
    form.academicYear = detail.academicYear
    form.semester = detail.semester
    form.courseId = detail.courseId
    form.studentIds = detail.studentIds ?? []
  }
  loadingDetail.value = false
}

function closeModal() {
  showModal.value = false
  isEditing.value = false
  editingId.value = null
}

async function handleSubmit() {
  formErrors.year = !form.year || form.year < 1 || form.year > 6
  formErrors.section = !form.section.trim()
  formErrors.academicYear = !form.academicYear || form.academicYear < 2000
  formErrors.semester = !form.semester
  formErrors.courseId = !isEditing.value && !form.courseId

  if (formErrors.year || formErrors.section || formErrors.academicYear || formErrors.semester || formErrors.courseId) {
    toast.error('Por favor, preencha todos os campos obrigatórios corretamente.')
    return
  }

  isEditing.value ? await handleUpdate() : await handleCreate()
}

async function handleCreate() {
  try {
    await cohortStore.createCohort({
      year: form.year,
      section: form.section.toUpperCase(),
      academicYear: form.academicYear,
      semester: form.semester as number,
      courseId: form.courseId as number,
      studentIds: [],
    })
    toast.success('Turma criada com sucesso!')
    closeModal()
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao criar turma.')
  }
}

async function handleUpdate() {
  if (!editingId.value) return
  try {
    await cohortStore.updateCohort(editingId.value, {
      year: form.year,
      section: form.section.toUpperCase(),
      academicYear: form.academicYear,
      semester: form.semester as number,
      studentIds: form.studentIds,
    })
    toast.success('Turma actualizada com sucesso!')
    closeModal()
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao actualizar turma.')
  }
}

async function handleDelete(id: number) {
  try {
    await cohortStore.deleteCohort(id)
    confirmDeleteId.value = null
    toast.success('Turma eliminada com sucesso!')
    fetchCohorts(currentPage.value)
  } catch {
    toast.error('Erro ao eliminar turma.')
  }
}
</script>
