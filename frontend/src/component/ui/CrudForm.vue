<template>
  <form
    @submit.prevent="handleSubmit"
    class="bg-white rounded-lg border border-gray-200 overflow-hidden"
  >
    <!-- Header -->
    <div class="px-6 py-4 border-b border-gray-200 bg-gray-50">
      <h2 class="text-lg font-semibold text-gray-900">{{ title }}</h2>
      <p v-if="subtitle" class="text-sm text-gray-500 mt-1">{{ subtitle }}</p>
    </div>

    <!-- Form Fields -->
    <div class="p-6 space-y-5">
      <div v-for="field in fields" :key="field.name">
        <label
          :for="field.name"
          class="block text-sm font-medium text-gray-700 mb-2"
        >
          {{ field.label || field.placeholder }}
        </label>
        <input
          :id="field.name"
          v-model="form[field.name]"
          :type="field.type"
          :placeholder="field.placeholder"
          class="w-full px-4 py-2.5 text-gray-900 border border-gray-300 rounded-lg
                 placeholder:text-gray-400
                 focus:outline-none focus:ring-2 focus:ring-blue-900 focus:border-transparent
                 transition"
        />
      </div>
    </div>

    <!-- Footer Actions -->
    <div class="px-6 py-4 border-t border-gray-200 bg-gray-50 flex justify-end gap-3">
      <button
        type="button"
        @click="$emit('cancel')"
        class="px-4 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg
               hover:bg-gray-50 transition"
      >
        Cancelar
      </button>
      <button
        type="submit"
        class="px-5 py-2.5 text-sm font-medium text-white bg-blue-900 rounded-lg
               hover:bg-blue-800 transition flex items-center gap-2"
      >
        <Save v-if="!isCreate" class="w-4 h-4" />
        <Plus v-else class="w-4 h-4" />
        {{ isCreate ? 'Criar' : 'Guardar' }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import { Save, Plus } from 'lucide-vue-next'

const props = defineProps<{
  fields: { name: string; type: string; placeholder: string; label?: string }[]
  data?: Record<string, any>
  title: string
  subtitle?: string
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
