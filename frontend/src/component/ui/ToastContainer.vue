<template>
  <div class="fixed top-4 right-4 z-50 space-y-2 max-w-sm w-full">
    <TransitionGroup name="toast">
      <div v-for="toast in toasts" :key="toast.id"
        :class="['bg-white rounded-xl shadow-lg border flex items-start gap-3 px-4 py-3', borderClass(toast.type)]">
        <!-- Coloured left accent -->
        <div :class="['w-1 self-stretch rounded-full shrink-0 -ml-1 mr-0.5', accentClass(toast.type)]" />

        <!-- Message -->
        <p class="flex-1 text-sm text-gray-700 font-medium leading-snug">{{ toast.message }}</p>

        <!-- Dismiss -->
        <button @click="remove(toast.id)" class="text-gray-300 hover:text-gray-500 transition shrink-0 mt-0.5">
          <X class="w-3.5 h-3.5" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { toasts, remove } = useToast()

const borderClass = (type: string) => ({
  success: 'border-green-100',
  error: 'border-red-100',
  warning: 'border-amber-100',
  info: 'border-blue-100',
}[type] ?? 'border-gray-100')

const accentClass = (type: string) => ({
  success: 'bg-green-400',
  error: 'bg-red-400',
  warning: 'bg-amber-400',
  info: 'bg-blue-900',
}[type] ?? 'bg-gray-300')

</script>

<style scoped>
.toast-enter-active {
  animation: slideIn 0.25s ease-out;
}

.toast-leave-active {
  animation: slideOut 0.2s ease-in;
}

@keyframes slideIn {
  from {
    transform: translateX(110%);
    opacity: 0;
  }

  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOut {
  from {
    transform: translateX(0);
    opacity: 1;
  }

  to {
    transform: translateX(110%);
    opacity: 0;
  }
}
</style>
