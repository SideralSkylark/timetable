<template>
  <div class="bg-white shadow rounded-md overflow-hidden">
    <table class="min-w-full divide-y divide-gray-200">
      <thead class="bg-gray-50">
        <tr>
          <th v-for="col in columns" :key="col.key" class="px-4 py-2 text-left text-sm font-medium text-gray-500">
            {{ col.label }}
          </th>
          <th class="px-4 py-2 text-left text-sm font-medium text-gray-500">Ações</th>
        </tr>
      </thead>
      <tbody class="bg-white divide-y divide-gray-200">
        <tr v-for="row in rows" :key="row.id">
          <td v-for="col in columns" :key="col.key" class="px-4 py-2">{{ row[col.key] }}</td>
          <td class="px-4 py-2 flex gap-2">
            <button @click="$emit('edit', row)" class="px-2 py-1 text-white bg-blue-600 rounded hover:bg-blue-700">Editar</button>
            <button @click="$emit('delete', row.id)" class="px-2 py-1 text-white bg-red-600 rounded hover:bg-red-700">Apagar</button>
          </td>
        </tr>
      </tbody>
    </table>

    <Pagination :page="currentPage" :totalPages="totalPages" @update:page="$emit('change-page', $event)" />
  </div>
</template>

<script setup lang="ts">
import Pagination from './Pagination.vue'

const props = defineProps<{
  columns: { key: string; label: string }[]
  rows: any[]
  currentPage: number
  totalPages: number
}>()

const emit = defineEmits<{
  (e: 'edit', row: any): void
  (e: 'delete', id: number): void
  (e: 'change-page', page: number): void
}>()
</script>

