<template>
  <div :class="['flex flex-col gap-1.5', block ? 'w-full' : '']">
    <label v-if="label" class="flex items-center gap-1.5 text-xs font-semibold text-slate-500 uppercase tracking-wider">
      <slot name="label-icon" />
      {{ label }}
    </label>
    <div class="relative group">
      <div v-if="$slots.icon" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-blue-900 transition-colors">
        <slot name="icon" />
      </div>
      <input
        v-bind="$attrs"
        :value="modelValue"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
        :class="[
          'w-full bg-white border border-slate-200 rounded-lg text-sm transition-all duration-200 focus:ring-2 focus:ring-blue-100 focus:border-blue-900 outline-none placeholder:text-slate-300 text-slate-700',
          $slots.icon ? 'pl-10' : 'px-3',
          $slots['right-icon'] ? 'pr-10' : 'px-3',
          error ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : '',
          sizeClasses[size]
        ]"
      />
      <div v-if="$slots['right-icon']" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">
        <slot name="right-icon" />
      </div>
    </div>
    <span v-if="error" class="text-[10px] font-medium text-red-600 flex items-center gap-1">
      <AlertCircle class="w-3 h-3" />
      {{ error }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { AlertCircle } from 'lucide-vue-next'

type InputSize = 'sm' | 'md' | 'lg'

interface Props {
  modelValue: string | number
  label?: string
  error?: string
  size?: InputSize
  block?: boolean
}

withDefaults(defineProps<Props>(), {
  modelValue: '',
  size: 'md',
  block: true
})

defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const sizeClasses: Record<InputSize, string> = {
  sm: 'py-1.5 h-8',
  md: 'py-2 h-10',
  lg: 'py-3 h-12 text-base'
}
</script>
