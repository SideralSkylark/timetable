<template>
  <div class="mb-3 flex items-center justify-between bg-red-50 border border-red-100 rounded-lg px-4 py-3">
    <span class="text-sm text-red-700" v-html="message"></span>
    <div class="flex gap-2">
      <button @click="$emit('cancel')"
        class="px-3 py-1.5 text-xs border border-gray-200 text-gray-500 rounded-md hover:bg-white transition">
        Cancelar
      </button>
      <button @click="$emit('confirm')"
        class="px-3 py-1.5 text-xs bg-red-600 text-white rounded-md hover:bg-red-700 transition font-medium">
        Eliminar
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  message: string
}>()

defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()
</script>
