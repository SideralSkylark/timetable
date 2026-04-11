<template>
  <form
    @submit.prevent="handleSubmit"
    class="bg-white rounded-[10px] border border-gray-100 shadow-sm overflow-hidden"
  >
    <!-- Header -->
    <div class="px-5 py-4 border-b border-gray-100 flex items-center gap-3">
      <div :class="isCreate ? 'bg-blue-50' : 'bg-amber-50'" class="p-2 rounded-md shrink-0">
        <Plus v-if="isCreate" class="w-4 h-4 text-blue-900" />
        <Save v-else class="w-4 h-4 text-amber-600" />
      </div>
      <div>
        <h2 class="text-base font-semibold text-gray-900">{{ title }}</h2>
        <p v-if="subtitle" class="text-xs text-gray-400 mt-0.5">{{ subtitle }}</p>
      </div>
    </div>

    <!-- Form fields -->
    <div class="p-5 space-y-4">
      <div v-for="field in fields" :key="field.name">
        <label :for="field.name" class="flex items-center gap-1.5 text-xs font-medium text-gray-500 mb-1.5">
          {{ field.label || field.placeholder }}
          <span v-if="field.required" class="text-blue-900">*</span>
        </label>

        <!-- Select -->
        <div v-if="field.type === 'select'" class="relative">
          <select
            :id="field.name"
            v-model="form[field.name]"
            :required="field.required"
            class="w-full px-3 py-2 pr-8 border border-gray-200 rounded-md text-sm text-gray-800 bg-white appearance-none focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition cursor-pointer"
          >
            <option v-if="field.placeholder && !field.required" :value="null" class="text-gray-400">
              {{ field.placeholder }}
            </option>
            <option v-if="field.placeholder && field.required" value="" disabled selected class="text-gray-400">
              {{ field.placeholder }}
            </option>
            <option v-for="option in field.options" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <ChevronDown class="w-3.5 h-3.5 text-gray-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
        </div>

        <!-- Input -->
        <input
          v-else
          :id="field.name"
          v-model="form[field.name]"
          :type="field.type"
          :placeholder="field.placeholder"
          :required="field.required"
          class="w-full px-3 py-2 border border-gray-200 rounded-md text-sm text-gray-800 focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none transition placeholder:text-gray-300"
        />
      </div>
    </div>

    <!-- Footer -->
    <div class="px-5 py-4 border-t border-gray-100 flex justify-end gap-2">
      <button
        type="button"
        @click="$emit('cancel')"
        class="px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition flex items-center gap-1.5"
      >
        <X class="w-3.5 h-3.5" />
        Cancelar
      </button>
      <button
        type="submit"
        class="px-4 py-2 bg-blue-900 text-white rounded-lg text-sm font-medium hover:bg-blue-800 transition flex items-center gap-1.5"
      >
        <Save v-if="!isCreate" class="w-3.5 h-3.5" />
        <Plus v-else class="w-3.5 h-3.5" />
        {{ isCreate ? 'Criar' : 'Guardar alterações' }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import { Save, Plus, ChevronDown, X } from 'lucide-vue-next'

interface FieldOption {
  label: string
  value: any
}

interface Field {
  name: string
  type: string
  placeholder: string
  label?: string
  options?: FieldOption[]
  required?: boolean
}

const props = defineProps<{
  fields: Field[]
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

const initializeForm = () => {
  Object.keys(form).forEach(key => delete form[key])
  if (props.data) {
    for (const key of Object.keys(props.data)) {
      form[key] = props.data[key]
    }
  } else {
    props.fields.forEach(field => {
      if (field.type === 'select') {
        form[field.name] = field.required ? '' : null
      } else if (field.type === 'number') {
        form[field.name] = 0
      } else {
        form[field.name] = ''
      }
    })
  }
}

watch(
  () => [props.data, props.fields],
  () => { initializeForm() },
  { immediate: true, deep: true }
)

const handleSubmit = () => {
  emit('submit', { ...form })
}
</script>
