<template>
  <div class="fixed top-4 right-4 z-50 space-y-3 max-w-md">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="[
          'rounded-lg shadow-lg p-4 flex items-start gap-3 border',
          getToastClasses(toast.type)
        ]"
      >
        <component
          :is="getIcon(toast.type)"
          :class="['w-5 h-5 flex-shrink-0 mt-0.5', getIconColor(toast.type)]"
        />
        <p :class="['flex-1 text-sm font-medium', getTextColor(toast.type)]">
          {{ toast.message }}
        </p>
        <button
          @click="remove(toast.id)"
          :class="['hover:opacity-70 transition flex-shrink-0', getIconColor(toast.type)]"
        >
          <X class="w-4 h-4" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { CheckCircle, XCircle, AlertCircle, Info, X } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const { toasts, remove } = useToast()

const getToastClasses = (type: string) => {
  const classes = {
    success: 'bg-green-50 border-green-200',
    error: 'bg-red-50 border-red-200',
    warning: 'bg-yellow-50 border-yellow-200',
    info: 'bg-blue-50 border-blue-200',
  }
  return classes[type as keyof typeof classes] || classes.info
}

const getIconColor = (type: string) => {
  const colors = {
    success: 'text-green-600',
    error: 'text-red-600',
    warning: 'text-yellow-600',
    info: 'text-blue-600',
  }
  return colors[type as keyof typeof colors] || colors.info
}

const getTextColor = (type: string) => {
  const colors = {
    success: 'text-green-800',
    error: 'text-red-800',
    warning: 'text-yellow-800',
    info: 'text-blue-800',
  }
  return colors[type as keyof typeof colors] || colors.info
}

const getIcon = (type: string) => {
  const icons = {
    success: CheckCircle,
    error: XCircle,
    warning: AlertCircle,
    info: Info,
  }
  return icons[type as keyof typeof icons] || Info
}
</script>

<style scoped>
.toast-enter-active {
  animation: slideIn 0.3s ease-out;
}

.toast-leave-active {
  animation: slideOut 0.3s ease-in;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
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
    transform: translateX(100%);
    opacity: 0;
  }
}
</style>
