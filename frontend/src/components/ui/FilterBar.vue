<template>
  <div class="mb-5">
    <div class="bg-white rounded-[10px] border border-slate-200 px-5 py-4">
      <div class="flex flex-wrap items-end gap-4">
        <slot name="filters" />
        <div class="flex-1 flex items-end justify-end">
          <slot name="actions">
            <button
              v-if="activeFilterCount > 0"
              @click="$emit('clear')"
              class="h-8 flex items-center gap-1.5 px-3 border border-gray-200 text-xs text-gray-500 rounded-lg hover:bg-gray-50 transition"
            >
              <X class="w-3.5 h-3.5" />
              Limpar filtros
              <span class="bg-blue-100 text-blue-800 text-xs rounded-full w-4 h-4 flex items-center justify-center font-medium leading-none ml-1">
                {{ activeFilterCount }}
              </span>
            </button>
          </slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { X } from 'lucide-vue-next'

defineProps<{
  activeFilterCount: number
}>()

defineEmits<{
  (e: 'clear'): void
}>()
</script>
