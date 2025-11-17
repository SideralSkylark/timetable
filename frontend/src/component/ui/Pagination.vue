<template>
  <div class="flex items-center justify-between py-4">
    <button
      class="px-4 py-2 border rounded-md disabled:opacity-50"
      :disabled="page === 0"
      @click="$emit('update:page', page - 1)"
    >
      Anterior
    </button>

    <span class="text-sm text-gray-600">
      Página {{ page + 1 }} de {{ totalPages }}
    </span>

    <button
      class="px-4 py-2 border rounded-md disabled:opacity-50"
      :disabled="page + 1 >= totalPages"
      @click="$emit('update:page', page + 1)"
    >
      Próximo
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  page: number
  totalPages: number
}>()

defineEmits(['update:page'])
</script>
