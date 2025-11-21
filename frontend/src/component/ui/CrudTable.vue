<template>
  <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
    <table class="min-w-full">
      <thead>
        <tr class="bg-gray-50 border-b border-gray-200">
          <th
            v-for="col in columns"
            :key="col.key"
            class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider"
          >
            {{ col.label }}
          </th>
          <th class="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">
            Ações
          </th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-100">
        <tr
          v-for="row in rows"
          :key="row.id"
          class="hover:bg-gray-50 transition"
        >
          <td
            v-for="col in columns"
            :key="col.key"
            class="px-6 py-4 text-sm text-gray-900"
          >
            {{ row[col.key] }}
          </td>
          <td class="px-6 py-4">
            <div class="flex items-center justify-end gap-2">
              <button
                @click="$emit('edit', row)"
                class="p-2 text-gray-400 hover:text-blue-900 hover:bg-blue-50 rounded-lg transition"
                title="Editar"
              >
                <Edit2 class="w-4 h-4" />
              </button>
              <button
                @click="$emit('delete', row.id)"
                class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition"
                title="Apagar"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </td>
        </tr>
        <tr v-if="rows.length === 0">
          <td
            :colspan="columns.length + 1"
            class="px-6 py-12 text-center text-gray-500"
          >
            Nenhum registo encontrado
          </td>
        </tr>
      </tbody>
    </table>

    <div class="border-t border-gray-200">
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
}>()

defineEmits<{
  (e: 'edit', row: any): void
  (e: 'delete', id: number): void
  (e: 'change-page', page: number): void
}>()
</script>
