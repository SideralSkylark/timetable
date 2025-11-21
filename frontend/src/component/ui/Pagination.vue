<template>
  <div class="flex items-center justify-between py-4 px-6 bg-white rounded-lg border border-gray-200">
    <button
      class="flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg transition
             enabled:hover:bg-blue-900 enabled:hover:text-white
             disabled:opacity-40 disabled:cursor-not-allowed
             text-gray-700 border border-gray-300"
      :disabled="page === 0"
      @click="$emit('update:page', page - 1)"
    >
      <ChevronLeft class="w-4 h-4" />
      Anterior
    </button>

    <div class="flex items-center gap-2">
      <button
        v-for="pageNum in visiblePages"
        :key="pageNum"
        class="w-10 h-10 text-sm font-medium rounded-lg transition"
        :class="pageNum === page + 1
          ? 'bg-blue-900 text-white'
          : 'text-gray-600 hover:bg-gray-100'"
        @click="$emit('update:page', pageNum - 1)"
      >
        {{ pageNum }}
      </button>
    </div>

    <button
      class="flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg transition
             enabled:hover:bg-blue-900 enabled:hover:text-white
             disabled:opacity-40 disabled:cursor-not-allowed
             text-gray-700 border border-gray-300"
      :disabled="page + 1 >= totalPages"
      @click="$emit('update:page', page + 1)"
    >
      Próximo
      <ChevronRight class="w-4 h-4" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

const props = defineProps<{
  page: number
  totalPages: number
}>()

defineEmits(['update:page'])

const visiblePages = computed(() => {
  const current = props.page + 1
  const total = props.totalPages
  const pages: number[] = []

  let start = Math.max(1, current - 2)
  let end = Math.min(total, current + 2)

  if (current <= 3) {
    end = Math.min(5, total)
  }
  if (current >= total - 2) {
    start = Math.max(1, total - 4)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})
</script>
