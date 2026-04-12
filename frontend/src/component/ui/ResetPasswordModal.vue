<template>
  <div class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-[60]"
    @click.self="$emit('close')">
    <div class="bg-white rounded-[10px] shadow-2xl max-w-sm w-full border border-gray-100 p-6 print-container">
      
      <div class="flex flex-col items-center text-center space-y-4">
        <div class="bg-amber-50 p-2 rounded-md">
          <KeyRound class="w-6 h-6 text-amber-600" />
        </div>
        
        <div>
          <h2 class="text-lg font-bold text-gray-900">Password reposta!</h2>
          <div class="mt-2 flex items-start gap-2 bg-red-50 border border-red-100 rounded-md px-3 py-2 text-red-700">
            <AlertCircle class="w-4 h-4 mt-0.5 shrink-0" />
            <p class="text-xs font-medium no-print">
              Esta password só é mostrada uma vez. Guarde-a num local seguro.
            </p>
          </div>
        </div>

        <div class="w-full bg-gray-50 border-2 border-dashed border-gray-200 rounded-md p-4 my-2 relative group">
          <span class="text-2xl font-mono font-bold tracking-wider text-blue-900 select-all print-only-text">
            {{ password }}
          </span>
        </div>

        <div class="grid grid-cols-2 gap-3 w-full no-print">
          <button
            @click="copyToClipboard"
            class="h-10 flex items-center justify-center gap-2 px-4 border border-gray-200 rounded-md text-sm font-medium hover:bg-gray-50 transition"
            :class="copyFeedback ? 'text-green-600 border-green-200 bg-green-50' : 'text-gray-600'"
          >
            <Copy v-if="!copyFeedback" class="w-4 h-4" />
            <Check v-else class="w-4 h-4" />
            {{ copyFeedback ? 'Copiado!' : 'Copiar' }}
          </button>
          <button
            @click="printPassword"
            class="h-10 flex items-center justify-center gap-2 px-4 border border-gray-200 rounded-md text-sm font-medium text-gray-600 hover:bg-gray-50 transition"
          >
            <Printer class="w-4 h-4" />
            Imprimir
          </button>
        </div>

        <button
          @click="$emit('close')"
          class="w-full h-10 bg-blue-900 text-white rounded-md text-sm font-semibold hover:bg-blue-800 transition no-print"
        >
          Fechar
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { KeyRound, AlertCircle, Copy, Check, Printer } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const props = defineProps<{
  password: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const toast = useToast()
const copyFeedback = ref(false)

const copyToClipboard = async () => {
  try {
    await navigator.clipboard.writeText(props.password)
    copyFeedback.value = true
    setTimeout(() => {
      copyFeedback.value = false
    }, 2000)
  } catch {
    toast.error('Erro ao copiar password')
  }
}

const printPassword = () => {
  window.print()
}
</script>

<style scoped>
@media print {
  body * {
    visibility: hidden;
  }
  .print-container, .print-container * {
    visibility: visible;
  }
  .print-container {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    border: none !important;
    box-shadow: none !important;
  }
  .no-print {
    display: none !important;
  }
  .print-only-text {
    font-size: 48pt !important;
    display: block !important;
    text-align: center !important;
    margin-top: 2in !important;
  }
}
</style>
