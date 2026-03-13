<template>
  <div class="min-h-screen bg-gray-50">

    <!-- Header -->
    <div class="mb-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="bg-blue-900 p-2.5 rounded-lg">
              <GraduationCap class="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 class="text-xl font-semibold text-gray-900">Gestão de cursos</h1>
              <p class="text-gray-400 text-sm">Gerir cursos e suas disciplinas</p>
            </div>
          </div>
          <button @click="openCreateModal"
            class="bg-blue-900 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-800 transition text-sm font-medium">
            <Plus class="w-4 h-4" />
            Novo curso
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-16 text-gray-400 text-sm gap-2">
      <Loader2 class="w-4 h-4 animate-spin" />
      A carregar cursos...
    </div>

    <!-- Course list -->
    <div v-else class="space-y-2">

      <!-- Empty state -->
      <div v-if="courses.length === 0"
        class="text-center py-16 bg-white rounded-xl border border-gray-100 shadow-sm">
        <BookOpen class="w-10 h-10 text-gray-300 mx-auto mb-3" />
        <p class="text-gray-500 text-sm font-medium">Nenhum curso registado</p>
        <button @click="openCreateModal" class="mt-3 text-blue-900 hover:underline text-sm">
          Criar primeiro curso
        </button>
      </div>

      <div v-for="course in courses" :key="course.id"
        class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">

        <!-- Course row -->
        <div class="px-5 py-4 flex items-center justify-between hover:bg-gray-50 transition group">
          <div class="flex items-center gap-3 flex-1 min-w-0">

            <!-- Expand toggle -->
            <button @click="toggleCourse(course)"
              class="text-gray-300 hover:text-blue-900 transition shrink-0">
              <ChevronDown v-if="course.expanded" class="w-4 h-4" />
              <ChevronRight v-else class="w-4 h-4" />
            </button>

            <!-- Icon -->
            <div class="bg-blue-50 p-2 rounded-lg shrink-0">
              <BookOpen class="w-4 h-4 text-blue-900" />
            </div>

            <!-- Info -->
            <div class="flex-1 min-w-0">
              <h3 class="font-semibold text-gray-800 text-sm">{{ course.name }}</h3>
              <div class="flex items-center gap-2 mt-0.5 flex-wrap">
                <span class="text-xs text-gray-400 flex items-center gap-1">
                  <User class="w-3 h-3" />
                  {{ course.coordinatorName || 'Sem coordenador' }}
                </span>
                <span class="text-gray-200">·</span>
                <span class="text-xs text-gray-400 flex items-center gap-1">
                  <BookOpen class="w-3 h-3" />
                  {{ course.subjectCount }} disciplina{{ course.subjectCount !== 1 ? 's' : '' }}
                </span>
                <span class="text-gray-200">·</span>
                <span class="text-xs text-gray-400">
                  {{ course.years }} ano{{ course.years !== 1 ? 's' : '' }}
                </span>
                <template v-if="course.expectedCohortsPerYear && Object.keys(course.expectedCohortsPerYear).length > 0">
                  <span class="text-gray-200">·</span>
                  <div class="flex items-center gap-1 flex-wrap">
                    <span v-for="(count, year) in course.expectedCohortsPerYear" :key="year"
                      class="text-xs px-1.5 py-0.5 bg-blue-50 text-blue-700 rounded-full border border-blue-100">
                      {{ year }}º: {{ count }}T
                    </span>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <!-- Actions (hover reveal) -->
          <div class="flex items-center gap-1.5 opacity-0 group-hover:opacity-100 transition shrink-0">
            <button @click="openEditModal(course)"
              class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-amber-600 hover:border-amber-200 hover:bg-amber-50 transition"
              title="Editar curso">
              <Edit2 class="w-3.5 h-3.5" />
            </button>
            <button @click="confirmDeleteCourseId = course.id"
              class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-red-600 hover:border-red-200 hover:bg-red-50 transition"
              title="Eliminar curso">
              <Trash2 class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        <!-- Delete confirmation banner (per course) -->
        <div v-if="confirmDeleteCourseId === course.id"
          class="flex items-center justify-between bg-red-50 border-t border-red-100 px-5 py-3">
          <span class="text-sm text-red-700">Tem a certeza que quer eliminar este curso?</span>
          <div class="flex gap-2">
            <button @click="confirmDeleteCourseId = null"
              class="px-3 py-1.5 text-xs border border-gray-200 text-gray-500 rounded-md hover:bg-white transition">
              Cancelar
            </button>
            <button @click="handleDeleteCourse(course.id)"
              class="px-3 py-1.5 text-xs bg-red-600 text-white rounded-md hover:bg-red-700 transition font-medium">
              Eliminar
            </button>
          </div>
        </div>

        <!-- Subjects panel -->
        <div v-if="course.expanded" class="border-t border-gray-100 bg-gray-50/60">
          <div class="px-5 py-4">

            <div class="flex items-center justify-between mb-3">
              <h4 class="text-xs font-medium text-gray-500 uppercase tracking-wide">Disciplinas</h4>
              <button @click="openDisciplineModal(course)"
                class="flex items-center gap-1 text-xs text-blue-900 hover:underline font-medium">
                <Plus class="w-3.5 h-3.5" />
                Adicionar disciplina
              </button>
            </div>

            <!-- Loading subjects -->
            <div v-if="course.loadingSubjects" class="text-gray-400 text-xs text-center py-6 flex items-center justify-center gap-1.5">
              <Loader2 class="w-3.5 h-3.5 animate-spin" />
              A carregar disciplinas...
            </div>

            <!-- Empty subjects -->
            <div v-else-if="course.disciplines.length === 0"
              class="text-gray-400 text-xs text-center py-6">
              Nenhuma disciplina registada
            </div>

            <!-- Subject cards -->
            <div v-else class="space-y-1.5">
              <div v-for="subject in course.disciplines" :key="subject.id"
                class="bg-white rounded-lg border px-4 py-3 flex items-center justify-between hover:border-blue-100 transition"
                :class="subject.fixedDaySession ? 'border-amber-100 bg-amber-50/30' : 'border-gray-100'">

                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <p class="text-sm font-medium text-gray-800">{{ subject.name }}</p>
                    <span v-if="subject.fixedDaySession"
                      class="inline-flex items-center gap-1 px-2 py-0.5 bg-amber-100 text-amber-700 rounded-full text-xs font-medium">
                      <Calendar class="w-3 h-3" />
                      Quarta-feira · A Equipa
                    </span>
                  </div>
                  <div class="flex items-center gap-2 mt-0.5 text-xs text-gray-400">
                    <span>{{ subject.credits }} créditos</span>
                    <span>·</span>
                    <span>{{ subject.targetYear }}º ano</span>
                    <span>·</span>
                    <span>{{ subject.targetSemester }}º semestre</span>
                  </div>
                  <div v-if="!subject.fixedDaySession && subject.eligibleTeachers?.length > 0"
                    class="mt-1.5 flex items-center gap-1.5 flex-wrap">
                    <span class="text-xs text-gray-400">Professores:</span>
                    <span v-for="teacher in subject.eligibleTeachers" :key="teacher.id"
                      class="inline-flex items-center gap-1 px-1.5 py-0.5 bg-blue-50 text-blue-800 rounded text-xs">
                      <User class="w-2.5 h-2.5" />{{ teacher.username }}
                    </span>
                  </div>
                  <div v-else-if="!subject.fixedDaySession" class="mt-1 text-xs text-gray-300">
                    Nenhum professor elegível
                  </div>
                </div>

                <div class="flex items-center gap-1.5 shrink-0 ml-3">
                  <template v-if="!subject.fixedDaySession">
                    <button @click="openEditDisciplineModal(subject, course)"
                      class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-amber-600 hover:border-amber-200 hover:bg-amber-50 transition">
                      <Edit2 class="w-3.5 h-3.5" />
                    </button>
                    <button @click="confirmDeleteSubjectId = subject.id; confirmDeleteSubjectCourse = course"
                      class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-red-600 hover:border-red-200 hover:bg-red-50 transition">
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </template>
                  <span v-else
                    class="text-xs text-amber-600 px-2 py-1 bg-amber-50 rounded-md border border-amber-100">
                    Automática
                  </span>
                </div>
              </div>

              <!-- Subject delete banner -->
              <div v-if="confirmDeleteSubjectId !== null && confirmDeleteSubjectCourse?.id === course.id"
                class="flex items-center justify-between bg-red-50 border border-red-100 rounded-lg px-4 py-3">
                <span class="text-sm text-red-700">Tem a certeza que quer eliminar esta disciplina?</span>
                <div class="flex gap-2">
                  <button @click="confirmDeleteSubjectId = null; confirmDeleteSubjectCourse = null"
                    class="px-3 py-1.5 text-xs border border-gray-200 text-gray-500 rounded-md hover:bg-white transition">
                    Cancelar
                  </button>
                  <button @click="handleDeleteDiscipline(confirmDeleteSubjectId!, confirmDeleteSubjectCourse!)"
                    class="px-3 py-1.5 text-xs bg-red-600 text-white rounded-md hover:bg-red-700 transition font-medium">
                    Eliminar
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Course Modal ── -->
    <div v-if="showCourseModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
      @click.self="closeCourseModal">
      <div class="bg-white rounded-xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto border border-gray-100">

        <div class="p-5 border-b border-gray-100 flex items-center gap-3">
          <div :class="editingCourse ? 'bg-amber-50' : 'bg-blue-50'" class="p-2 rounded-lg">
            <Edit v-if="editingCourse" class="w-4 h-4 text-amber-600" />
            <Plus v-else class="w-4 h-4 text-blue-900" />
          </div>
          <div>
            <h2 class="text-base font-semibold text-gray-900">
              {{ editingCourse ? 'Editar curso' : 'Novo curso' }}
            </h2>
            <p v-if="editingCourse" class="text-xs text-gray-400 mt-0.5">{{ editingCourse.name }}</p>
          </div>
        </div>

        <form @submit.prevent="handleCourseSubmit" class="p-5 space-y-4">

          <!-- Nome -->
          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <Tag class="w-3.5 h-3.5" />
              Nome do curso <span class="text-blue-900">*</span>
            </label>
            <input v-model="courseForm.name" type="text" required
              placeholder="Ex: Engenharia Informática"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800 placeholder:text-gray-300" />
          </div>

          <!-- Coordenador -->
          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <User class="w-3.5 h-3.5" />
              Coordenador <span class="text-blue-900">*</span>
            </label>
            <div class="relative">
              <select v-model.number="courseForm.coordinatorId" required
                class="w-full px-3 py-2 pr-8 border border-gray-200 rounded-lg text-sm text-gray-800 appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer">
                <option value="" disabled>Selecionar coordenador</option>
                <option v-for="c in coordinators" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
              <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <!-- Duração -->
          <div>
            <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
              <Calendar class="w-3.5 h-3.5" />
              Duração (anos) <span class="text-blue-900">*</span>
            </label>
            <input v-model.number="courseForm.years" type="number" min="1" max="6" required
              @change="syncCohortRows"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800" />
          </div>

          <!-- Business simulation toggle -->
          <div class="flex items-center justify-between px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition">
            <div class="flex items-center gap-3">
              <div class="bg-amber-50 p-1.5 rounded-lg">
                <BookOpen class="w-3.5 h-3.5 text-amber-600" />
              </div>
              <div>
                <p class="text-sm font-medium text-gray-800">Simulação empresarial</p>
                <p class="text-xs text-gray-400">Inclui Simulação Empresarial I (3º ano) e II (4º ano)</p>
              </div>
            </div>
            <button type="button"
              @click="courseForm.hasBusinessSimulation = !courseForm.hasBusinessSimulation"
              :class="courseForm.hasBusinessSimulation ? 'bg-amber-500 border-amber-500' : 'bg-gray-200 border-gray-200'"
              class="relative w-10 h-5 rounded-full border-2 transition-colors duration-200 shrink-0">
              <span
                :class="courseForm.hasBusinessSimulation ? 'translate-x-5' : 'translate-x-0'"
                class="absolute left-0.5 top-0.5 w-3.5 h-3.5 bg-white rounded-full shadow transition-transform duration-200" />
            </button>
          </div>

          <!-- Expected cohorts per year -->
          <div>
            <div class="flex items-center justify-between mb-2">
              <label class="flex items-center gap-1.5 text-xs font-medium text-gray-500">
                <UsersIcon class="w-3.5 h-3.5" />
                Turmas esperadas por ano curricular
              </label>
              <span class="text-xs text-gray-400">máx. por semestre</span>
            </div>
            <div class="border border-gray-200 rounded-lg overflow-hidden">
              <div class="bg-gray-50 grid grid-cols-2 px-4 py-2 border-b border-gray-100">
                <span class="text-xs font-medium text-gray-400 uppercase tracking-wide">Ano</span>
                <span class="text-xs font-medium text-gray-400 uppercase tracking-wide">Turmas / sem.</span>
              </div>
              <div v-for="row in cohortRows" :key="row.year"
                class="grid grid-cols-2 items-center px-4 py-2.5 border-b border-gray-100 last:border-0 hover:bg-gray-50 transition">
                <span class="text-sm text-gray-700">{{ row.year }}º ano</span>
                <div class="flex items-center gap-2">
                  <button type="button" @click="row.count = Math.max(0, row.count - 1)"
                    class="w-6 h-6 rounded border border-gray-200 flex items-center justify-center text-gray-400 hover:border-blue-900 hover:text-blue-900 transition">
                    <Minus class="w-3 h-3" />
                  </button>
                  <input v-model.number="row.count" type="number" min="0" max="10"
                    class="w-12 text-center px-1 py-1 border border-gray-200 rounded-md text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none" />
                  <button type="button" @click="row.count++"
                    class="w-6 h-6 rounded border border-gray-200 flex items-center justify-center text-gray-400 hover:border-blue-900 hover:text-blue-900 transition">
                    <PlusIcon class="w-3 h-3" />
                  </button>
                </div>
              </div>
            </div>
            <p class="text-xs text-gray-400 mt-1.5">
              Define quantas turmas são esperadas por semestre para cada ano curricular.
            </p>
          </div>

          <div class="flex gap-2 pt-1">
            <button type="button" @click="closeCourseModal"
              class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center justify-center gap-1.5">
              <X class="w-3.5 h-3.5" />
              Cancelar
            </button>
            <button type="submit"
              class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg text-sm font-medium hover:bg-blue-800 transition flex items-center justify-center gap-1.5">
              <Save v-if="editingCourse" class="w-3.5 h-3.5" />
              <Check v-else class="w-3.5 h-3.5" />
              {{ editingCourse ? 'Guardar alterações' : 'Criar curso' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- ── Discipline Modal ── -->
    <div v-if="showDisciplineModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50"
      @click.self="closeDisciplineModal">
      <div class="bg-white rounded-xl shadow-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto border border-gray-100">

        <div class="p-5 border-b border-gray-100 flex items-center gap-3">
          <div :class="editingDiscipline ? 'bg-amber-50' : 'bg-blue-50'" class="p-2 rounded-lg">
            <Edit v-if="editingDiscipline" class="w-4 h-4 text-amber-600" />
            <Plus v-else class="w-4 h-4 text-blue-900" />
          </div>
          <div>
            <h2 class="text-base font-semibold text-gray-900">
              {{ editingDiscipline ? 'Editar disciplina' : 'Nova disciplina' }}
            </h2>
            <p class="text-xs text-gray-400 mt-0.5">{{ selectedCourse?.name }}</p>
          </div>
        </div>

        <div class="p-5 space-y-4">

          <!-- Nome -->
          <div>
            <label class="text-xs font-medium text-gray-500 mb-1.5 block">
              Nome da disciplina <span class="text-blue-900">*</span>
            </label>
            <input v-model="disciplineForm.name" type="text" placeholder="Ex: Cálculo I"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800 placeholder:text-gray-300" />
          </div>

          <!-- Créditos -->
          <div>
            <label class="text-xs font-medium text-gray-500 mb-1.5 block">
              Créditos <span class="text-blue-900">*</span>
            </label>
            <input v-model.number="disciplineForm.credits" type="number" min="1" placeholder="Ex: 6"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800 placeholder:text-gray-300" />
          </div>

          <!-- Ano / Semestre -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Ano <span class="text-blue-900">*</span>
              </label>
              <input v-model.number="disciplineForm.targetYear" type="number" min="1"
                :max="selectedCourse?.years || 1"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800" />
            </div>
            <div>
              <label class="text-xs font-medium text-gray-500 mb-1.5 block">
                Semestre <span class="text-blue-900">*</span>
              </label>
              <input v-model.number="disciplineForm.targetSemester" type="number" min="1" max="2"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition text-gray-800" />
            </div>
          </div>

          <!-- Teacher selection -->
          <div>
            <label class="text-xs font-medium text-gray-500 mb-1.5 block">
              Professores elegíveis <span class="text-blue-900">*</span>
            </label>

            <!-- Selected teachers -->
            <div v-if="disciplineForm.selectedTeachers.length > 0" class="flex flex-wrap gap-1.5 mb-2">
              <div v-for="teacher in disciplineForm.selectedTeachers" :key="teacher.id"
                class="inline-flex items-center gap-1.5 px-2.5 py-1 bg-blue-900 text-white rounded-lg text-xs">
                <User class="w-3 h-3" />
                {{ teacher.username }}
                <button @click="removeTeacher(teacher.id)"
                  class="hover:bg-blue-800 rounded p-0.5 transition">
                  <X class="w-3 h-3" />
                </button>
              </div>
            </div>

            <!-- Search -->
            <div class="relative mb-2">
              <input v-model="teacherSearchQuery" type="text" placeholder="Pesquisar professores..."
                class="w-full px-3 py-2 pl-8 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition placeholder:text-gray-300" />
              <Search class="w-3.5 h-3.5 text-gray-400 absolute left-2.5 top-2.5" />
            </div>

            <!-- Teacher list -->
            <div class="border border-gray-200 rounded-lg max-h-44 overflow-y-auto">
              <div v-if="loadingTeachers" class="p-4 text-center text-gray-400 text-xs flex items-center justify-center gap-1.5">
                <Loader2 class="w-3.5 h-3.5 animate-spin" />
                A carregar professores...
              </div>
              <div v-else-if="filteredTeachers.length === 0"
                class="p-4 text-center text-gray-400 text-xs">
                Nenhum professor encontrado
              </div>
              <div v-else>
                <button v-for="teacher in filteredTeachers" :key="teacher.id" type="button"
                  @click="toggleTeacher(teacher)"
                  class="w-full px-3 py-2.5 flex items-center gap-3 hover:bg-gray-50 border-b border-gray-100 last:border-0 transition"
                  :class="isTeacherSelected(teacher.id) ? 'bg-blue-50' : ''">
                  <div class="w-4 h-4 rounded border-2 flex items-center justify-center shrink-0"
                    :class="isTeacherSelected(teacher.id) ? 'border-blue-900 bg-blue-900' : 'border-gray-300'">
                    <Check v-if="isTeacherSelected(teacher.id)" class="w-2.5 h-2.5 text-white" />
                  </div>
                  <div class="text-left">
                    <p class="text-sm font-medium text-gray-800">{{ teacher.username }}</p>
                    <p class="text-xs text-gray-400">{{ teacher.email }}</p>
                  </div>
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="px-5 py-4 border-t border-gray-100 flex gap-2">
          <button type="button" @click="closeDisciplineModal"
            class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center justify-center gap-1.5">
            <X class="w-3.5 h-3.5" />
            Cancelar
          </button>
          <button type="button" @click="submitDiscipline" :disabled="!isDisciplineFormValid"
            class="flex-1 px-4 py-2 bg-blue-900 text-white rounded-lg text-sm font-medium hover:bg-blue-800 transition flex items-center justify-center gap-1.5 disabled:opacity-50 disabled:cursor-not-allowed">
            <Check class="w-3.5 h-3.5" />
            {{ editingDiscipline ? 'Guardar alterações' : 'Criar disciplina' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { courseService } from '@/services/courseService'
import { subjectService } from '@/services/subjectService'
import { teacherService } from '@/services/teacherService'
import { useToast } from '@/composables/useToast'
import type { CourseListResponse, CoordinatorOption } from '@/services/dto/course'
import {
  GraduationCap, Plus, Edit, Edit2, Trash2,
  ChevronDown, ChevronRight, BookOpen, User,
  Tag, Calendar, Minus, X, Save, Check,
  Search, Loader2, Users as UsersIcon,
} from 'lucide-vue-next'
import { Plus as PlusIcon } from 'lucide-vue-next'

const toast = useToast()

// ── State ─────────────────────────────────────────────────────────
const courses = ref<any[]>([])
const coordinators = ref<CoordinatorOption[]>([])
const teachers = ref<any[]>([])
const loading = ref(false)
const loadingTeachers = ref(false)
const showCourseModal = ref(false)
const showDisciplineModal = ref(false)
const editingCourse = ref<any>(null)
const selectedCourse = ref<any>(null)
const editingDiscipline = ref<any>(null)
const teacherSearchQuery = ref('')
const confirmDeleteCourseId = ref<number | null>(null)
const confirmDeleteSubjectId = ref<number | null>(null)
const confirmDeleteSubjectCourse = ref<any>(null)

const cohortRows = ref<{ year: number; count: number }[]>([])

const courseForm = reactive({
  name: '',
  coordinatorId: '' as number | '',
  years: 4,
  hasBusinessSimulation: false,
})

const disciplineForm = ref({
  name: '',
  credits: 0,
  targetYear: 1,
  targetSemester: 1,
  selectedTeachers: [] as any[],
})

// ── Computed ──────────────────────────────────────────────────────
const filteredTeachers = computed(() => {
  const q = teacherSearchQuery.value.toLowerCase().trim()
  if (!q) return teachers.value
  return teachers.value.filter(t =>
    t.username.toLowerCase().includes(q) || t.email.toLowerCase().includes(q)
  )
})

const isDisciplineFormValid = computed(() =>
  disciplineForm.value.name.trim() !== '' &&
  disciplineForm.value.credits > 0 &&
  disciplineForm.value.targetYear > 0 &&
  disciplineForm.value.targetSemester > 0 &&
  disciplineForm.value.selectedTeachers.length > 0
)

function buildExpectedCohortsMap(): Record<number, number> {
  const map: Record<number, number> = {}
  for (const row of cohortRows.value) {
    if (row.count > 0) map[row.year] = row.count
  }
  return map
}

function syncCohortRows() {
  const years = courseForm.years || 0
  const existing = new Map(cohortRows.value.map(r => [r.year, r.count]))
  cohortRows.value = Array.from({ length: years }, (_, i) => ({
    year: i + 1,
    count: existing.get(i + 1) ?? 0,
  }))
}

// ── Data loading ──────────────────────────────────────────────────
async function loadCourses() {
  loading.value = true
  try {
    const page = await courseService.getAll(0, 50)
    courses.value = page.content.map((c: CourseListResponse) => ({
      ...c,
      disciplines: [],
      expanded: false,
      loadingSubjects: false,
    }))
  } catch {
    toast.error('Erro ao carregar cursos.')
  } finally {
    loading.value = false
  }
}

async function loadCoordinators() {
  try {
    const page = await courseService.getCoordinators()
    coordinators.value = page.content
  } catch {
    coordinators.value = []
  }
}

async function loadTeachers() {
  loadingTeachers.value = true
  try {
    const page = await teacherService.getAll(0, 1000)
    teachers.value = page.content
  } finally {
    loadingTeachers.value = false
  }
}

onMounted(() => Promise.all([loadCourses(), loadCoordinators()]))

// ── Course expand ─────────────────────────────────────────────────
async function toggleCourse(course: any) {
  course.expanded = !course.expanded
  if (course.expanded && course.disciplines.length === 0) {
    await loadSubjects(course)
  }
}

async function loadSubjects(course: any, force = false) {
  if (course.loadingSubjects || (!force && course.disciplines.length > 0)) return
  course.loadingSubjects = true
  try {
    const page = await subjectService.getAllByCourse(course.id, 0, 100)
    course.disciplines = page.content
  } finally {
    course.loadingSubjects = false
  }
}

// ── Course modal ──────────────────────────────────────────────────
function openCreateModal() {
  editingCourse.value = null
  courseForm.name = ''
  courseForm.coordinatorId = ''
  courseForm.years = 4
  courseForm.hasBusinessSimulation = false
  cohortRows.value = Array.from({ length: 4 }, (_, i) => ({ year: i + 1, count: 0 }))
  loadCoordinators()
  showCourseModal.value = true
}

function openEditModal(course: any) {
  editingCourse.value = course
  courseForm.name = course.name
  courseForm.coordinatorId = course.coordinatorId
  courseForm.years = course.years
  courseForm.hasBusinessSimulation = course.hasBusinessSimulation ?? false
  const map: Record<number, number> = course.expectedCohortsPerYear ?? {}
  cohortRows.value = Array.from({ length: course.years }, (_, i) => ({
    year: i + 1,
    count: map[i + 1] ?? 0,
  }))
  loadCoordinators()
  showCourseModal.value = true
}

function closeCourseModal() {
  showCourseModal.value = false
  editingCourse.value = null
}

async function handleCourseSubmit() {
  const payload = {
    name: courseForm.name,
    coordinatorId: courseForm.coordinatorId as number,
    years: courseForm.years,
    expectedCohortsPerYear: buildExpectedCohortsMap(),
    hasBusinessSimulation: courseForm.hasBusinessSimulation,
  }
  try {
    if (editingCourse.value) {
      await courseService.update(editingCourse.value.id, payload)
      toast.success('Curso actualizado com sucesso!')
    } else {
      await courseService.create(payload)
      toast.success('Curso criado com sucesso!')
    }
    closeCourseModal()
    await loadCourses()
  } catch {
    toast.error('Erro ao guardar curso.')
  }
}

async function handleDeleteCourse(id: number) {
  try {
    await courseService.delete(id)
    confirmDeleteCourseId.value = null
    toast.success('Curso eliminado com sucesso!')
    await loadCourses()
  } catch {
    toast.error('Erro ao eliminar curso.')
  }
}

// ── Discipline modal ──────────────────────────────────────────────
function resetDisciplineForm() {
  disciplineForm.value = { name: '', credits: 0, targetYear: 1, targetSemester: 1, selectedTeachers: [] }
  teacherSearchQuery.value = ''
}

async function openDisciplineModal(course: any) {
  selectedCourse.value = course
  editingDiscipline.value = null
  resetDisciplineForm()
  if (teachers.value.length === 0) await loadTeachers()
  showDisciplineModal.value = true
}

async function openEditDisciplineModal(discipline: any, course: any) {
  selectedCourse.value = course
  editingDiscipline.value = { ...discipline }
  if (teachers.value.length === 0) await loadTeachers()
  disciplineForm.value = {
    name: discipline.name,
    credits: discipline.credits,
    targetYear: discipline.targetYear,
    targetSemester: discipline.targetSemester,
    selectedTeachers: (discipline.eligibleTeachers ?? []).map((t: any) => ({ ...t })),
  }
  showDisciplineModal.value = true
}

function closeDisciplineModal() {
  showDisciplineModal.value = false
  editingDiscipline.value = null
  selectedCourse.value = null
  resetDisciplineForm()
}

async function submitDiscipline() {
  if (!isDisciplineFormValid.value) return
  const data = {
    name: disciplineForm.value.name,
    credits: disciplineForm.value.credits,
    targetYear: disciplineForm.value.targetYear,
    targetSemester: disciplineForm.value.targetSemester,
    courseId: selectedCourse.value.id,
    eligibleTeacherIds: disciplineForm.value.selectedTeachers.map((t: any) => t.id),
  }
  try {
    if (editingDiscipline.value) {
      await subjectService.update(editingDiscipline.value.id, data)
      toast.success('Disciplina actualizada!')
    } else {
      await subjectService.create(data)
      const idx = courses.value.findIndex(c => c.id === selectedCourse.value.id)
      if (idx !== -1) courses.value[idx].subjectCount++
      toast.success('Disciplina criada!')
    }
    closeDisciplineModal()
    await loadSubjects(selectedCourse.value, true)
  } catch {
    toast.error('Erro ao guardar disciplina.')
  }
}

async function handleDeleteDiscipline(id: number, course: any) {
  try {
    await subjectService.delete(id)
    confirmDeleteSubjectId.value = null
    confirmDeleteSubjectCourse.value = null
    await loadSubjects(course, true)
    const idx = courses.value.findIndex(c => c.id === course.id)
    if (idx !== -1) courses.value[idx].subjectCount--
    toast.success('Disciplina eliminada!')
  } catch {
    toast.error('Erro ao eliminar disciplina.')
  }
}

// ── Teacher helpers ───────────────────────────────────────────────
const isTeacherSelected = (id: number) =>
  disciplineForm.value.selectedTeachers.some(t => t.id === id)

function toggleTeacher(teacher: any) {
  const idx = disciplineForm.value.selectedTeachers.findIndex(t => t.id === teacher.id)
  if (idx > -1) disciplineForm.value.selectedTeachers.splice(idx, 1)
  else disciplineForm.value.selectedTeachers.push(teacher)
}

function removeTeacher(id: number) {
  const idx = disciplineForm.value.selectedTeachers.findIndex(t => t.id === id)
  if (idx > -1) disciplineForm.value.selectedTeachers.splice(idx, 1)
}
</script>
