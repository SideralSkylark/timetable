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

        <!-- Select/Dropdown Field -->
        <div v-if="field.type === 'select'" class="relative">
          <select
            :id="field.name"
            v-model="form[field.name]"
            :required="field.required"
            class="w-full px-4 py-2.5 text-gray-900 border border-gray-300 rounded-lg
                   appearance-none bg-white
                   focus:outline-none focus:ring-2 focus:ring-blue-900 focus:border-transparent
                   transition cursor-pointer"
          >
            <option 
              v-if="field.placeholder && !field.required" 
              :value="null"
              class="text-gray-400"
            >
              {{ field.placeholder }}
            </option>
            <option
              v-if="field.placeholder && field.required"
              value=""
              disabled
              selected
              class="text-gray-400"
            >
              {{ field.placeholder }}
            </option>
            <option
              v-for="option in field.options"
              :key="option.value"
              :value="option.value"
              class="text-gray-900"
            >
              {{ option.label }}
            </option>
          </select>
          <!-- Dropdown Icon -->
          <ChevronDown class="w-5 h-5 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
        </div>

        <!-- Regular Input Fields -->
        <input
          v-else
          :id="field.name"
          v-model="form[field.name]"
          :type="field.type"
          :placeholder="field.placeholder"
          :required="field.required"
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
import { Save, Plus, ChevronDown } from 'lucide-vue-next'

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

// Inicializar campos do formulário
const initializeForm = () => {
  // Limpar formulário primeiro
  Object.keys(form).forEach(key => delete form[key])
  
  // Se há dados, popular com eles
  if (props.data) {
    for (const key of Object.keys(props.data)) {
      form[key] = props.data[key]
    }
  } else {
    // Se não há dados (modo criação), inicializar campos vazios
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

// Watch para mudanças nos dados ou campos
watch(
  () => [props.data, props.fields],
  () => {
    initializeForm()
  },
  { immediate: true, deep: true }
)

const handleSubmit = () => {
  emit('submit', { ...form })
}
</script>
