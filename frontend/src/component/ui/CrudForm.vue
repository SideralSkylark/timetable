<template>
  <form @submit.prevent="handleSubmit" class="p-4 border rounded-md bg-white shadow-sm">
    <h2 class="text-lg font-semibold mb-2">{{ title }}</h2>
    <div v-for="field in fields" :key="field.name" class="mb-2">
      <input
        v-model="form[field.name]"
        :type="field.type"
        :placeholder="field.placeholder"
        class="border rounded px-2 py-1 w-full"
      />
    </div>
    <div class="flex justify-end gap-2">
      <button type="button" @click="$emit('cancel')" class="px-4 py-1 bg-gray-300 rounded hover:bg-gray-400">Cancelar</button>
      <button type="submit" class="px-4 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700">{{ isCreate ? 'Criar' : 'Salvar' }}</button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'

const props = defineProps<{
  fields: { name: string; type: string; placeholder: string }[]
  data?: Record<string, any>
  title: string
  isCreate?: boolean
}>()

const emit = defineEmits<{
  (e: 'submit', data: Record<string, any>): void
  (e: 'cancel'): void
}>()

const form = reactive<Record<string, any>>({})

watch(
  () => props.data,
  (val) => {
    if (val) {
      for (const key of Object.keys(val)) form[key] = val[key]
    }
  },
  { immediate: true }
)

const handleSubmit = () => emit('submit', { ...form })
</script>
