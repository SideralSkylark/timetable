<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
    <table class="w-full text-sm">
      <thead>
        <tr class="border-b border-gray-100">
          <th
            v-for="col in columns"
            :key="col.key"
            class="text-left px-5 py-3 text-xs font-medium text-gray-400 uppercase tracking-wide"
          >
            {{ col.label }}
          </th>
          <th class="px-5 py-3" />
        </tr>
      </thead>
      <tbody>
        <tr v-if="rows.length === 0">
          <td :colspan="columns.length + 1" class="text-center py-16 text-gray-400 text-sm">
            <slot name="empty">Nenhum registo encontrado</slot>
          </td>
        </tr>
        <tr
          v-for="row in rows"
          :key="row.id"
          class="border-b border-gray-50 last:border-0 hover:bg-gray-50 transition-colors group"
        >
          <td
            v-for="col in columns"
            :key="col.key"
            class="px-5 py-3.5"
          >
            <!-- Custom cell slot: name pattern is `cell-{key}` -->
            <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
              <span class="text-gray-700">{{ row[col.key] }}</span>
            </slot>
          </td>
          <td class="px-5 py-3.5">
            <div class="flex gap-2 justify-end">
              <button
                v-if="canEdit ? canEdit(row) : true"
                @click="$emit('edit', row)"
                class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-blue-900 hover:border-blue-200 hover:bg-blue-50 transition opacity-0 group-hover:opacity-100"
                title="Editar"
              >
                <Edit2 class="w-3.5 h-3.5" />
              </button>
              <button
                v-if="canDelete ? canDelete(row) : true"
                @click="$emit('delete', row.id)"
                class="p-1.5 border border-gray-200 rounded-md text-gray-400 hover:text-red-600 hover:border-red-200 hover:bg-red-50 transition opacity-0 group-hover:opacity-100"
                title="Eliminar"
              >
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="border-t border-gray-100">
      <Pagination
        :page="currentPage"
        :totalPages="totalPages"
        @update:page="$emit('change-page', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Edit2, Trash2 } from 'lucide-vue-next'
import Pagination from './Pagination.vue'

defineProps<{
  columns: { key: string; label: string }[]
  rows: any[]
  currentPage: number
  totalPages: number
  canEdit?: (row: any) => boolean
  canDelete?: (row: any) => boolean
}>()

defineEmits<{
  (e: 'edit', row: any): void
  (e: 'delete', id: number): void
  (e: 'change-page', page: number): void
}>()
</script>
