<template>
  <div class="flex items-center justify-between px-5 py-3">

    <!-- Prev -->
    <button
      :disabled="page === 0"
      @click="$emit('update:page', page - 1)"
      class="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md border transition"
      :class="page === 0
        ? 'border-gray-100 text-gray-300 cursor-not-allowed'
        : 'border-gray-200 text-gray-500 hover:bg-gray-50 hover:border-gray-300'"
    >
      <ChevronLeft class="w-3.5 h-3.5" />
      Anterior
    </button>

    <!-- Page numbers -->
    <div class="flex items-center gap-1">
      <button
        v-for="pageNum in visiblePages"
        :key="pageNum"
        @click="$emit('update:page', pageNum - 1)"
        class="w-7 h-7 text-xs font-medium rounded-md transition flex items-center justify-center"
        :class="pageNum === page + 1
          ? 'bg-blue-900 text-white'
          : 'text-gray-500 hover:bg-gray-100'"
      >
        {{ pageNum }}
      </button>
    </div>

    <!-- Next -->
    <button
      :disabled="page + 1 >= totalPages"
      @click="$emit('update:page', page + 1)"
      class="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md border transition"
      :class="page + 1 >= totalPages
        ? 'border-gray-100 text-gray-300 cursor-not-allowed'
        : 'border-gray-200 text-gray-500 hover:bg-gray-50 hover:border-gray-300'"
    >
      Próximo
      <ChevronRight class="w-3.5 h-3.5" />
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
  let start = Math.max(1, current - 2)
  let end = Math.min(total, current + 2)
  if (current <= 3) end = Math.min(5, total)
  if (current >= total - 2) start = Math.max(1, total - 4)
  const pages: number[] = []
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})
</script>
