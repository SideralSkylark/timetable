<template>
  <button
    :type="type"
    :disabled="loading || disabled"
    :class="[
      'inline-flex items-center justify-center gap-2 rounded-lg text-sm font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed',
      variantClasses[variant],
      sizeClasses[size],
      block ? 'w-full' : ''
    ]"
  >
    <Loader2 v-if="loading" class="w-4 h-4 animate-spin" />
    <slot v-else name="icon" />
    <slot />
  </button>
</template>

<script setup lang="ts">
import { Loader2 } from 'lucide-vue-next'

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'danger' | 'ghost'
type ButtonSize = 'sm' | 'md' | 'lg'

interface Props {
  type?: 'button' | 'submit' | 'reset'
  variant?: ButtonVariant
  size?: ButtonSize
  loading?: boolean
  disabled?: boolean
  block?: boolean
}

withDefaults(defineProps<Props>(), {
  type: 'button',
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  block: false
})

const variantClasses: Record<ButtonVariant, string> = {
  primary: 'bg-blue-900 text-white hover:bg-blue-800 focus:ring-blue-500 shadow-sm',
  secondary: 'bg-slate-100 text-slate-900 hover:bg-slate-200 focus:ring-slate-500',
  outline: 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 hover:border-slate-300 focus:ring-slate-500',
  danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500 shadow-sm',
  ghost: 'bg-transparent text-slate-600 hover:bg-slate-100 focus:ring-slate-500'
}

const sizeClasses: Record<ButtonSize, string> = {
  sm: 'px-3 py-1.5 text-xs',
  md: 'px-4 py-2',
  lg: 'px-6 py-3 text-base'
}
</script>
